package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.config.DeepSeekProperties;
import com.hlju.bookstore.dto.AiChatMessage;
import com.hlju.bookstore.dto.AiChatRequest;
import com.hlju.bookstore.dto.AiChatResponse;
import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.repository.BookRepository;
import com.hlju.bookstore.service.AiCustomerService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiCustomerServiceImpl implements AiCustomerService {
    private static final int MAX_CONTEXT_BOOKS = 6;
    private static final int MAX_HISTORY_MESSAGES = 8;

    private final BookRepository bookRepository;
    private final DeepSeekProperties deepSeekProperties;
    private final RestClient restClient;

    public AiCustomerServiceImpl(BookRepository bookRepository, DeepSeekProperties deepSeekProperties) {
        this.bookRepository = bookRepository;
        this.deepSeekProperties = deepSeekProperties;
        this.restClient = RestClient.create();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request, String username, String role) {
        String question = normalize(request == null ? null : request.message());
        if (!StringUtils.hasText(question)) {
            return AiChatResponse.failed("请输入想咨询的问题");
        }

        List<Book> candidateBooks = bookRepository.findAiCandidates(question, MAX_CONTEXT_BOOKS);
        if (!hasApiKey()) {
            return AiChatResponse.fallback(
                    buildLocalReply(question, candidateBooks, "当前未配置 DeepSeek API Key，我先根据书库给你本地推荐。"),
                    candidateBooks,
                    "未配置 DeepSeek API Key"
            );
        }

        try {
            String reply = callDeepSeek(question, request.history(), candidateBooks, username, role);
            return AiChatResponse.success(reply, candidateBooks, "DEEPSEEK");
        } catch (Exception error) {
            return AiChatResponse.fallback(
                    buildLocalReply(question, candidateBooks, "DeepSeek 暂时连接失败，我先根据书库给你本地推荐。"),
                    candidateBooks,
                    error.getMessage()
            );
        }
    }

    private String callDeepSeek(String question,
                                List<AiChatMessage> history,
                                List<Book> candidateBooks,
                                String username,
                                String role) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        for (AiChatMessage item : safeHistory(history)) {
            messages.add(Map.of("role", normalizeRole(item.role()), "content", limit(item.content(), 800)));
        }
        messages.add(Map.of("role", "user", "content", buildUserPrompt(question, candidateBooks, username, role)));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", deepSeekProperties.getModel());
        body.put("messages", messages);
        body.put("temperature", deepSeekProperties.getTemperature());
        body.put("max_tokens", deepSeekProperties.getMaxTokens());
        body.put("stream", false);

        Map<String, Object> response = restClient.post()
                .uri(chatUrl())
                .header("Authorization", "Bearer " + deepSeekProperties.getApiKey().trim())
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        String content = extractAssistantContent(response);
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("DeepSeek 返回内容为空");
        }
        return content.trim();
    }

    private String buildSystemPrompt() {
        return """
                你是 Bookstore Cloud 在线图书商城的智能客服。
                你的语气友好、专业、简洁，默认使用中文回答。
                你可以根据系统提供的候选图书，为用户做购书推荐、学习路线建议、库存/价格解释、订单和评论入口指引。
                推荐图书时必须优先依据候选图书，不要编造书名、价格、库存和作者。
                如果候选图书不足以回答，请明确说明，并建议用户换关键词或联系人工客服。
                你不能直接下单、支付、取消订单或发布评论；用户想购买时，引导其点击推荐图书的“加入购物车”并在购物车结算。
                用户想评价时，引导其从右上角个人菜单进入“我的订单”，在已支付/已发货/已完成订单的图书明细里评价。
                """;
    }

    private String buildUserPrompt(String question, List<Book> candidateBooks, String username, String role) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("当前用户：").append(StringUtils.hasText(username) ? username : "未登录游客")
                .append("，角色：").append(StringUtils.hasText(role) ? role : "USER").append("\n\n");
        prompt.append("候选图书：\n");
        if (candidateBooks.isEmpty()) {
            prompt.append("暂无匹配图书。\n");
        } else {
            for (int i = 0; i < candidateBooks.size(); i++) {
                Book book = candidateBooks.get(i);
                prompt.append(i + 1)
                        .append(". ID=").append(book.getId())
                        .append("，《").append(book.getTitle()).append("》")
                        .append("，作者：").append(nullToEmpty(book.getAuthor()))
                        .append("，分类：").append(nullToEmpty(book.getCategory()))
                        .append("，价格：").append(book.getPrice())
                        .append("，库存：").append(book.getStock())
                        .append("，简介：").append(limit(book.getDescription(), 220))
                        .append("\n");
            }
        }
        prompt.append("\n用户问题：").append(question);
        return prompt.toString();
    }

    private String buildLocalReply(String question, List<Book> candidateBooks, String prefix) {
        StringBuilder reply = new StringBuilder(prefix).append("\n\n");
        if (candidateBooks.isEmpty()) {
            reply.append("我暂时没有在当前书库里找到特别匹配“").append(question)
                    .append("”的图书。你可以换成更具体的关键词，例如 Java、Spring Boot、MySQL、算法、Vue、小说等。");
            return reply.toString();
        }

        reply.append("根据你的问题“").append(question).append("”，我建议优先看看：\n");
        int count = Math.min(3, candidateBooks.size());
        for (int i = 0; i < count; i++) {
            Book book = candidateBooks.get(i);
            reply.append(i + 1)
                    .append(". 《").append(book.getTitle()).append("》")
                    .append("（").append(nullToEmpty(book.getCategory())).append("，￥").append(book.getPrice())
                    .append("，库存 ").append(book.getStock()).append("）");
            if (StringUtils.hasText(book.getDescription())) {
                reply.append("：").append(limit(book.getDescription(), 90));
            }
            reply.append("\n");
        }
        reply.append("\n如果你想购买，可以点击推荐卡片里的“加入购物车”，再到右侧购物车结算。");
        return reply.toString();
    }

    private List<AiChatMessage> safeHistory(List<AiChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        int from = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        return history.subList(from, history.size()).stream()
                .filter(item -> item != null && StringUtils.hasText(item.content()))
                .filter(item -> "user".equals(normalizeRole(item.role())) || "assistant".equals(normalizeRole(item.role())))
                .toList();
    }

    private String normalizeRole(String role) {
        if ("assistant".equalsIgnoreCase(role)) {
            return "assistant";
        }
        return "user";
    }

    @SuppressWarnings("unchecked")
    private String extractAssistantContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object choicesObject = response.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return "";
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choice)) {
            return "";
        }
        Object messageObject = choice.get("message");
        if (!(messageObject instanceof Map<?, ?> message)) {
            return "";
        }
        Object content = message.get("content");
        return content == null ? "" : content.toString();
    }

    private String chatUrl() {
        String baseUrl = deepSeekProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "https://api.deepseek.com";
        }
        baseUrl = baseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/chat/completions";
    }

    private boolean hasApiKey() {
        String apiKey = deepSeekProperties.getApiKey();
        return StringUtils.hasText(apiKey) && !"your-deepseek-api-key".equalsIgnoreCase(apiKey.trim());
    }

    private String normalize(String value) {
        return limit(value == null ? "" : value.trim(), 1000);
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "...";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
