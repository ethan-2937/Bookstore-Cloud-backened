package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.config.DeepSeekProperties;
import com.hlju.bookstore.dto.AiChatMessage;
import com.hlju.bookstore.dto.AiChatRequest;
import com.hlju.bookstore.dto.AiChatResponse;
import com.hlju.bookstore.dto.AiToolAction;
import com.hlju.bookstore.dto.AiToolResult;
import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.entity.FaqKnowledge;
import com.hlju.bookstore.repository.BookRepository;
import com.hlju.bookstore.repository.FaqKnowledgeRepository;
import com.hlju.bookstore.service.AiCustomerService;
import com.hlju.bookstore.service.OrderService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiCustomerServiceImpl implements AiCustomerService {
    private static final int MAX_CONTEXT_BOOKS = 6;
    private static final int MAX_CONTEXT_FAQS = 5;
    private static final int MAX_HISTORY_MESSAGES = 8;

    private final BookRepository bookRepository;
    private final FaqKnowledgeRepository faqKnowledgeRepository;
    private final OrderService orderService;
    private final DeepSeekProperties deepSeekProperties;
    private final RestClient restClient;

    public AiCustomerServiceImpl(BookRepository bookRepository,
                                 FaqKnowledgeRepository faqKnowledgeRepository,
                                 OrderService orderService,
                                 DeepSeekProperties deepSeekProperties) {
        this.bookRepository = bookRepository;
        this.faqKnowledgeRepository = faqKnowledgeRepository;
        this.orderService = orderService;
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
        List<FaqKnowledge> matchedFaqs = faqKnowledgeRepository.searchRelevant(question, MAX_CONTEXT_FAQS);
        List<AiToolResult> toolResults = buildToolResults(question, username, role, candidateBooks, matchedFaqs);
        List<AiToolAction> toolActions = buildToolActions(question, username, candidateBooks);

        if (!hasApiKey()) {
            return AiChatResponse.fallback(
                    buildLocalReply(question, candidateBooks, matchedFaqs, toolResults, toolActions,
                            "当前未配置 DeepSeek API Key，我先根据书库和客服知识库给你本地推荐。"),
                    candidateBooks,
                    matchedFaqs,
                    toolActions,
                    toolResults,
                    "未配置 DeepSeek API Key"
            );
        }

        try {
            String reply = callDeepSeek(question, request.history(), candidateBooks, matchedFaqs, toolResults, toolActions, username, role);
            return AiChatResponse.success(reply, candidateBooks, matchedFaqs, toolActions, toolResults, "DEEPSEEK");
        } catch (Exception error) {
            return AiChatResponse.fallback(
                    buildLocalReply(question, candidateBooks, matchedFaqs, toolResults, toolActions,
                            "DeepSeek 暂时连接失败，我先根据书库和客服知识库给你本地推荐。"),
                    candidateBooks,
                    matchedFaqs,
                    toolActions,
                    toolResults,
                    error.getMessage()
            );
        }
    }

    private String callDeepSeek(String question,
                                List<AiChatMessage> history,
                                List<Book> candidateBooks,
                                List<FaqKnowledge> matchedFaqs,
                                List<AiToolResult> toolResults,
                                List<AiToolAction> toolActions,
                                String username,
                                String role) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        for (AiChatMessage item : safeHistory(history)) {
            messages.add(Map.of("role", normalizeRole(item.role()), "content", limit(item.content(), 800)));
        }
        messages.add(Map.of("role", "user", "content",
                buildUserPrompt(question, candidateBooks, matchedFaqs, toolResults, toolActions, username, role)));

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

    private List<AiToolResult> buildToolResults(String question,
                                                String username,
                                                String role,
                                                List<Book> candidateBooks,
                                                List<FaqKnowledge> matchedFaqs) {
        List<AiToolResult> results = new ArrayList<>();
        if (!matchedFaqs.isEmpty()) {
            results.add(new AiToolResult(
                    "FAQ_SEARCH",
                    "客服知识库检索",
                    "已从客服知识库中找到 " + matchedFaqs.size() + " 条相关说明。",
                    Map.of("count", matchedFaqs.size())
            ));
        }
        if (!candidateBooks.isEmpty()) {
            results.add(new AiToolResult(
                    "BOOK_SEARCH",
                    "图书检索",
                    "已从图书库中找到 " + candidateBooks.size() + " 本候选图书。",
                    Map.of("count", candidateBooks.size())
            ));
        }
        if (isOrderQuery(question)) {
            if (StringUtils.hasText(username) && !"ADMIN".equalsIgnoreCase(role)) {
                List<Map<String, Object>> orders = orderService.findMyOrders(username);
                results.add(new AiToolResult(
                        "ORDER_QUERY",
                        "订单查询",
                        orders.isEmpty() ? "当前账号暂无订单。" : "已查询到最近 " + Math.min(orders.size(), 3) + " 个订单。",
                        Map.of("orders", orders.stream().limit(3).toList())
                ));
            } else {
                results.add(new AiToolResult(
                        "ORDER_QUERY",
                        "订单查询",
                        "查询个人订单需要先登录普通用户账号。",
                        Map.of("loginRequired", true)
                ));
            }
        }
        return results;
    }

    private List<AiToolAction> buildToolActions(String question, String username, List<Book> candidateBooks) {
        List<AiToolAction> actions = new ArrayList<>();
        if (!candidateBooks.isEmpty() && isPurchaseIntent(question)) {
            List<Map<String, Object>> items = candidateBooks.stream()
                    .limit(3)
                    .map(book -> Map.<String, Object>of(
                            "bookId", book.getId(),
                            "title", book.getTitle(),
                            "price", book.getPrice() == null ? BigDecimal.ZERO : book.getPrice(),
                            "stock", book.getStock() == null ? 0 : book.getStock(),
                            "quantity", 1
                    ))
                    .toList();
            actions.add(new AiToolAction(
                    "CREATE_ORDER_DRAFT",
                    "加入推荐书并去结算",
                    "把推荐图书加入购物车，由用户确认收货信息后再提交订单。",
                    true,
                    Map.of("items", items)
            ));
        }
        if (isSupportIntent(question)) {
            actions.add(new AiToolAction(
                    "CREATE_SUPPORT_TICKET",
                    "创建客服工单",
                    StringUtils.hasText(username) ? "根据当前问题创建客服工单，等待管理员回复。" : "登录后可创建客服工单。",
                    true,
                    Map.of(
                            "subject", buildTicketSubject(question),
                            "content", question,
                            "loginRequired", !StringUtils.hasText(username)
                    )
            ));
        }
        if (isOrderQuery(question)) {
            actions.add(new AiToolAction(
                    "OPEN_ORDERS",
                    "查看我的订单",
                    "打开订单中心查看订单状态、支付、取消和评价入口。",
                    false,
                    Map.of()
            ));
        }
        if (isReviewIntent(question)) {
            actions.add(new AiToolAction(
                    "OPEN_ORDERS",
                    "去订单里评价",
                    "评论需要从已支付、已发货或已完成订单的图书明细中发起。",
                    false,
                    Map.of()
            ));
        }
        return actions;
    }

    private String buildSystemPrompt() {
        return """
                你是 Bookstore Cloud 在线图书商城的智能客服。
                你的语气友好、专业、简洁，默认使用中文回答。
                系统会提供候选图书、客服知识库 FAQ、已查询的工具结果以及可由前端确认执行的工具动作。
                回答必须优先依据系统提供的图书、FAQ 和工具结果，不要编造书名、价格、库存、订单状态或规则。
                如果工具动作涉及下单、创建工单、评价等写操作，要提醒用户需要点击页面按钮并二次确认。
                你不能声称已经自动下单、自动支付、自动发布评论；只能说明可以通过页面按钮继续操作。
                如果候选信息不足以回答，请明确说明，并建议用户换关键词或联系人工客服。
                """;
    }

    private String buildUserPrompt(String question,
                                   List<Book> candidateBooks,
                                   List<FaqKnowledge> matchedFaqs,
                                   List<AiToolResult> toolResults,
                                   List<AiToolAction> toolActions,
                                   String username,
                                   String role) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("当前用户：").append(StringUtils.hasText(username) ? username : "未登录游客")
                .append("，角色：").append(StringUtils.hasText(role) ? role : "USER").append("\n\n");
        appendFaqs(prompt, matchedFaqs);
        appendBooks(prompt, candidateBooks);
        appendToolResults(prompt, toolResults);
        appendToolActions(prompt, toolActions);
        prompt.append("\n用户问题：").append(question);
        return prompt.toString();
    }

    private String buildLocalReply(String question,
                                   List<Book> candidateBooks,
                                   List<FaqKnowledge> matchedFaqs,
                                   List<AiToolResult> toolResults,
                                   List<AiToolAction> toolActions,
                                   String prefix) {
        StringBuilder reply = new StringBuilder(prefix).append("\n\n");
        if (!matchedFaqs.isEmpty()) {
            reply.append("我先查到这些客服知识：\n");
            for (int i = 0; i < Math.min(2, matchedFaqs.size()); i++) {
                FaqKnowledge faq = matchedFaqs.get(i);
                reply.append(i + 1).append(". ").append(faq.getQuestion()).append("：")
                        .append(limit(faq.getAnswer(), 120)).append("\n");
            }
            reply.append("\n");
        }
        if (!candidateBooks.isEmpty()) {
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
            reply.append("\n");
        }
        for (AiToolResult result : toolResults) {
            if ("ORDER_QUERY".equals(result.type())) {
                reply.append(result.content()).append("\n");
            }
        }
        if (!toolActions.isEmpty()) {
            reply.append("我已经为你准备了可确认操作按钮，你可以在本条消息下方继续操作。");
        } else if (candidateBooks.isEmpty() && matchedFaqs.isEmpty()) {
            reply.append("我暂时没有找到特别匹配的信息。你可以换成更具体的关键词，例如 Java、Spring Boot、订单评价、退换货或人工客服。");
        }
        return reply.toString();
    }

    private void appendFaqs(StringBuilder prompt, List<FaqKnowledge> matchedFaqs) {
        prompt.append("客服知识库 FAQ：\n");
        if (matchedFaqs.isEmpty()) {
            prompt.append("暂无匹配 FAQ。\n");
            return;
        }
        for (int i = 0; i < matchedFaqs.size(); i++) {
            FaqKnowledge faq = matchedFaqs.get(i);
            prompt.append(i + 1).append(". [").append(faq.getCategory()).append("] ")
                    .append(faq.getQuestion()).append("：").append(limit(faq.getAnswer(), 260)).append("\n");
        }
        prompt.append("\n");
    }

    private void appendBooks(StringBuilder prompt, List<Book> candidateBooks) {
        prompt.append("候选图书：\n");
        if (candidateBooks.isEmpty()) {
            prompt.append("暂无匹配图书。\n");
            return;
        }
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
        prompt.append("\n");
    }

    private void appendToolResults(StringBuilder prompt, List<AiToolResult> toolResults) {
        prompt.append("已执行工具结果：\n");
        if (toolResults.isEmpty()) {
            prompt.append("暂无。\n");
            return;
        }
        for (AiToolResult result : toolResults) {
            prompt.append("- ").append(result.title()).append("：").append(result.content()).append("\n");
        }
        prompt.append("\n");
    }

    private void appendToolActions(StringBuilder prompt, List<AiToolAction> toolActions) {
        prompt.append("可确认工具动作：\n");
        if (toolActions.isEmpty()) {
            prompt.append("暂无。\n");
            return;
        }
        for (AiToolAction action : toolActions) {
            prompt.append("- ").append(action.type()).append("：").append(action.label())
                    .append("。").append(action.description()).append("\n");
        }
        prompt.append("\n");
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

    private boolean isPurchaseIntent(String question) {
        return containsAny(question, "买", "购买", "下单", "加入购物车", "结算", "帮我买");
    }

    private boolean isSupportIntent(String question) {
        return containsAny(question, "人工", "客服", "工单", "投诉", "反馈", "联系管理员", "解决问题");
    }

    private boolean isOrderQuery(String question) {
        return containsAny(question, "订单", "物流", "发货", "支付", "取消", "状态", "评价", "评论");
    }

    private boolean isReviewIntent(String question) {
        return containsAny(question, "评价", "评论", "打分", "写评语", "发布评论");
    }

    private boolean containsAny(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildTicketSubject(String question) {
        String content = limit(question, 28);
        return "智能客服转人工：" + content;
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
