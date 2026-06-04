INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT 'Java核心技术 卷I', 'Cay S. Horstmann', '编程开发', 89.00, 36, '', '系统讲解 Java 语法、面向对象、集合、泛型、异常、并发等基础内容，适合作为 Java 后端入门和进阶参考。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Java核心技术 卷I' AND author = 'Cay S. Horstmann');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT 'Spring Boot实战', 'Craig Walls', '编程开发', 69.00, 28, '', '围绕 Spring Boot Web 开发、配置、测试和部署展开，适合构建企业级后端项目。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Spring Boot实战' AND author = 'Craig Walls');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '深入理解计算机系统', 'Randal E. Bryant', '计算机基础', 139.00, 18, '', '从程序员视角理解计算机系统，覆盖数据表示、体系结构、链接、内存、并发和网络。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '深入理解计算机系统' AND author = 'Randal E. Bryant');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '算法图解', 'Aditya Y. Bhargava', '算法', 49.80, 45, '', '用图示和示例解释搜索、排序、递归、动态规划、图算法等基础算法概念。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '算法图解' AND author = 'Aditya Y. Bhargava');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '数据结构与算法分析', 'Mark Allen Weiss', '算法', 78.00, 22, '', '系统介绍常见数据结构、复杂度分析、树、图、散列、堆和算法设计思想。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '数据结构与算法分析' AND author = 'Mark Allen Weiss');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT 'MySQL必知必会', 'Ben Forta', '数据库', 39.00, 52, '', '面向初学者的 MySQL 查询、排序、过滤、聚合、连接、子查询和数据维护指南。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'MySQL必知必会' AND author = 'Ben Forta');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '高性能MySQL', 'Baron Schwartz', '数据库', 128.00, 16, '', '讲解 MySQL 架构、索引优化、查询优化、复制、备份和性能调优实践。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '高性能MySQL' AND author = 'Baron Schwartz');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT 'Vue.js设计与实现', '霍春阳', '前端开发', 89.00, 30, '', '从响应式系统、组件、渲染器和编译器角度理解 Vue 3 的核心设计。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Vue.js设计与实现' AND author = '霍春阳');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT 'JavaScript高级程序设计', 'Matt Frisbie', '前端开发', 119.00, 24, '', '覆盖 JavaScript 语言基础、对象、函数、异步、DOM、BOM 和现代前端开发基础。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'JavaScript高级程序设计' AND author = 'Matt Frisbie');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '人月神话', 'Frederick P. Brooks', '软件工程', 59.00, 34, '', '软件工程经典作品，讨论项目管理、复杂度、团队协作和软件开发中的常见误区。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '人月神话' AND author = 'Frederick P. Brooks');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '代码整洁之道', 'Robert C. Martin', '软件工程', 68.00, 27, '', '讲解如何写出可读、可维护、可测试的代码，适合团队开发和代码评审场景。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '代码整洁之道' AND author = 'Robert C. Martin');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '设计模式', 'Erich Gamma', '软件工程', 79.00, 20, '', '经典设计模式参考书，介绍创建型、结构型和行为型模式及其适用场景。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '设计模式' AND author = 'Erich Gamma');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '活着', '余华', '文学小说', 45.00, 50, '', '以朴素有力的文字讲述普通人的命运与时代变迁，是当代中文文学代表作品之一。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '活着' AND author = '余华');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '围城', '钱锺书', '文学小说', 49.00, 38, '', '中国现代文学经典，以幽默讽刺的笔法描写知识分子的生活与情感困境。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '围城' AND author = '钱锺书');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '三体', '刘慈欣', '科幻', 58.00, 42, '', '中国科幻代表作品之一，围绕文明、宇宙社会学和人类命运展开宏大叙事。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '三体' AND author = '刘慈欣');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '银河帝国：基地', 'Isaac Asimov', '科幻', 52.00, 26, '', '阿西莫夫经典科幻系列开篇，讲述心理史学、帝国衰落与文明延续。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '银河帝国：基地' AND author = 'Isaac Asimov');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '原则', 'Ray Dalio', '商业管理', 88.00, 32, '', '作者总结个人成长、组织管理、决策机制和长期主义原则，适合管理和自我提升阅读。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '原则' AND author = 'Ray Dalio');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '从0到1', 'Peter Thiel', '商业管理', 46.00, 40, '', '讨论创新、垄断、创业、产品和技术公司从无到有的商业思考。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '从0到1' AND author = 'Peter Thiel');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '乌合之众', 'Gustave Le Bon', '社会科学', 36.00, 44, '', '社会心理学经典作品，讨论群体心理、情绪传染和大众行为特征。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '乌合之众' AND author = 'Gustave Le Bon');

INSERT INTO books (title, author, category, price, stock, cover_url, description)
SELECT '枪炮、病菌与钢铁', 'Jared Diamond', '社会科学', 72.00, 21, '', '从地理、农业、技术和疾病等角度解释人类社会发展的差异。'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = '枪炮、病菌与钢铁' AND author = 'Jared Diamond');
