USE study_compass;

INSERT INTO app_user (username, nickname, email, role)
SELECT 'demo_user', '演示用户', 'demo@example.com', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE username = 'demo_user');

INSERT INTO learning_goal
(user_id, title, description, start_date, target_date, weekly_hours, current_level, status)
SELECT id, '系统学习 Java Web', '完成 Spring Boot、MyBatis 与前后端分离实践', CURRENT_DATE,
       DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY), 8, '掌握 Java 基础语法', 'ACTIVE'
FROM app_user u
WHERE u.username = 'demo_user'
  AND NOT EXISTS (
      SELECT 1 FROM learning_goal g
      WHERE g.user_id = u.id AND g.title = '系统学习 Java Web'
  );

INSERT INTO study_record
(user_id, goal_id, task_id, study_date, duration_minutes, content, reflection, focus_score)
SELECT u.id, g.id, t.id, CURRENT_DATE, 55, '梳理 Spring MVC 请求处理流程',
       '能够说清 Controller、Service 与 Mapper 的职责边界。', 4
FROM app_user u
JOIN learning_goal g ON g.user_id = u.id AND g.title = '系统学习 Java Web'
LEFT JOIN learning_task t ON t.goal_id = g.id AND t.title = '完成第一个 RESTful 接口'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (
      SELECT 1 FROM study_record r
      WHERE r.user_id = u.id AND r.study_date = CURRENT_DATE
        AND r.content = '梳理 Spring MVC 请求处理流程'
  );

INSERT INTO user_preference
(user_id, weekly_target_hours, preferred_session_minutes, preferred_study_period, ai_personalization)
SELECT id, 10, 45, 'EVENING', TRUE
FROM app_user u
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM user_preference p WHERE p.user_id = u.id);

INSERT INTO learning_task
(goal_id, user_id, title, description, priority, status, due_date, estimated_minutes)
SELECT g.id, g.user_id, '完成第一个 RESTful 接口', '实现学习目标的查询与新增接口',
       'HIGH', 'IN_PROGRESS', DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 90
FROM learning_goal g
JOIN app_user u ON u.id = g.user_id
WHERE u.username = 'demo_user'
  AND g.title = '系统学习 Java Web'
  AND NOT EXISTS (
      SELECT 1 FROM learning_task t
      WHERE t.goal_id = g.id AND t.title = '完成第一个 RESTful 接口'
  );

INSERT INTO learning_resource
(user_id, goal_id, title, type, source_url, description, tags)
SELECT u.id, g.id, 'Spring Boot 官方参考文档', 'DOCUMENT',
       'https://docs.spring.io/spring-boot/index.html',
       '用于核对自动配置、Web 与数据访问相关用法，优先以官方文档为准。',
       'Spring Boot,官方文档,后端'
FROM app_user u
JOIN learning_goal g ON g.user_id = u.id AND g.title = '系统学习 Java Web'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (
      SELECT 1 FROM learning_resource r
      WHERE r.user_id = u.id AND r.title = 'Spring Boot 官方参考文档'
  );

INSERT INTO learning_resource
(user_id, goal_id, title, type, source_url, description, tags)
SELECT u.id, g.id, 'MyBatis 3 中文文档', 'ARTICLE',
       'https://mybatis.org/mybatis-3/zh_CN/index.html',
       '整理 Mapper XML、动态 SQL 与参数绑定时查阅。',
       'MyBatis,SQL,数据访问'
FROM app_user u
JOIN learning_goal g ON g.user_id = u.id AND g.title = '系统学习 Java Web'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (
      SELECT 1 FROM learning_resource r
      WHERE r.user_id = u.id AND r.title = 'MyBatis 3 中文文档'
  );

INSERT INTO learning_resource
(user_id, goal_id, title, type, source_url, description, tags)
SELECT u.id, g.id, 'RESTful 接口设计笔记', 'DOCUMENT', NULL,
       '统一资源命名、HTTP 方法、状态码和错误响应格式，作为项目接口自查清单。',
       'REST,接口规范,课程笔记'
FROM app_user u
JOIN learning_goal g ON g.user_id = u.id AND g.title = '系统学习 Java Web'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (
      SELECT 1 FROM learning_resource r
      WHERE r.user_id = u.id AND r.title = 'RESTful 接口设计笔记'
  );
