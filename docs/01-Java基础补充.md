# 第 1 篇：Java 基础补充——看懂项目代码的最低门槛

> 你已掌握：变量、循环、if-else、方法定义、类的创建。但是看项目代码时会遇到泛型、注解、Lambda、Stream 这些没学过的东西。这篇专门补这五个知识点。

---

## 1. 泛型（Generics）

### 是什么
把类型当作参数传递。`List<String>` 意思是"这个列表里只能放字符串"。

### 为什么需要
没有泛型时：
```java
List list = new ArrayList();
list.add("hello");
list.add(123);        // 也能加进去！编译器不报错
String s = (String) list.get(0);  // 必须强转，写错了运行时才报错
```

有泛型后：
```java
List<String> list = new ArrayList<>();
list.add("hello");
// list.add(123);     // 编译就报错！IDE 会标红
String s = list.get(0);  // 不需要强转
```

### 项目中哪里用到了
几乎到处都是。举几个例子：

```java
// Result.java - 统一返回结果
public class Result<T> {        // <T> 表示"任意类型"
    private T data;             // data 的类型由调用者决定

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }
}

// 使用时：
Result<UserVO> r1 = Result.ok(userVO);      // T = UserVO
Result<String> r2 = Result.ok("hello");      // T = String
Result<List<NewsVO>> r3 = Result.ok(list);   // T = List<NewsVO>
```

```java
// BaseMapper.java - MyBatis-Plus 的泛型接口
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper<User> 的意思是"这个 Mapper 操作的是 User 表"
    // 继承了 selectById、insert、updateById 等方法，泛型参数自动确定为 User
}
```

### 面试可能问
- "泛型的作用是什么？" → 编译期类型检查，避免运行时 ClassCastException
- "泛型擦除是什么？" → Java 编译后泛型信息被擦除，运行时 List<String> 和 List<Integer> 是同一个类

---

## 2. 注解（Annotation）

### 是什么
给代码贴标签。`@Override` 告诉编译器"这个方法是重写父类的"。

### 项目中常见的注解

**Spring 框架注解（最重要）**：

| 注解 | 位置 | 作用 | 大白话 |
|------|------|------|--------|
| `@SpringBootApplication` | NewsApplication.java | 标记启动类 | "我是程序入口" |
| `@RestController` | Controller 类上 | 标记为 REST 接口类 | "这个类对外暴露 API" |
| `@Service` | Service 类上 | 标记为业务逻辑类 | "这个类处理业务" |
| `@Component` | 工具类上 | 标记为 Spring 管理的 Bean | "Spring 帮我创建和管理这个对象" |
| `@Autowired` | 字段上 | 自动注入依赖 | "Spring 帮我把这个字段赋值" |
| `@Bean` | @Configuration 类的方法上 | 手动创建 Bean | "用这个方法创建的对象交给 Spring 管理" |
| `@Value("${...}")` | 字段上 | 读取配置文件 | "从 application.yml 取这个值" |

**数据相关注解**：

| 注解 | 作用 |
|------|------|
| `@Data` (Lombok) | 自动生成 getter/setter/toString/equals/hashCode |
| `@TableName("users")` (MyBatis-Plus) | 指定实体类对应的数据库表名 |
| `@TableId(type = IdType.AUTO)` | 主键自增 |
| `@TableField(fill = FieldFill.INSERT)` | 插入时自动填充（如创建时间） |

**校验注解**：

| 注解 | 作用 |
|------|------|
| `@NotBlank(message = "用户名不能为空")` | 字符串不能为空或全空格 |
| `@Size(min = 2, max = 50)` | 字符串长度限制 |
| `@Email` | 必须是合法邮箱格式 |
| `@Valid` | 触发校验 |

**安全注解**：

| 注解 | 作用 |
|------|------|
| `@PreAuthorize("hasRole('ADMIN')")` | 只有管理员能调用这个方法 |
| `@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")` | 管理员或审核员能调用 |

**自定义注解（本项目的）**：

| 注解 | 作用 |
|------|------|
| `@LogOperation(value = "登录系统")` | 标记这个方法需要记录操作日志 |
| `@RateLimit(key = "login", limit = 10, window = 60)` | 限制这个方法 60 秒内最多调用 10 次 |

### 注解怎么工作的？
以 `@LogOperation` 为例：
1. 定义注解 `@interface LogOperation { String value(); }`
2. 写一个 AOP 切面类 `LogAspect`，用 `@Around("@annotation(logOperation)")` 拦截所有带这个注解的方法
3. 方法执行时，AOP 在方法执行前后插入日志记录逻辑

```java
// 1. 定义
public @interface LogOperation {
    String value() default "";
}

// 2. 使用
@LogOperation(value = "登录系统")
public Result login(...) { ... }

// 3. AOP 拦截
@Around("@annotation(logOperation)")
public Object around(ProceedingJoinPoint jp, LogOperation op) {
    // 方法执行前
    Object result = jp.proceed();  // 执行原方法
    // 方法执行后：记录日志
    saveLog(op.value());
    return result;
}
```

---

## 3. 反射（Reflection）

### 是什么
在运行时动态获取类的信息、调用方法、修改字段。正常代码是"编译时就知道要调什么"，反射是"运行时才决定调什么"。

### 项目中哪里用到了
- Spring 的 `@Autowired`：Spring 扫描到你的字段，用反射给它赋值
- MyBatis-Plus 的 `selectById(1L)`：框架用反射把数据库查出来的 ResultSet 变成 User 对象
- AOP：运行时动态创建代理对象，在方法前后插入逻辑

### 面试可能问
- "反射的优缺点？" → 灵活但性能较低，破坏封装性
- "反射怎么用？" → Class.forName()、getDeclaredMethod()、method.invoke()

---

## 4. Lambda 表达式

### 是什么
一种简洁的写法，把方法当参数传递。

```java
// 传统写法：匿名内部类
new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("hello");
    }
}).start();

// Lambda 写法：一样的功能，代码少很多
new Thread(() -> System.out.println("hello")).start();
```

### 项目中哪里用到了

```java
// LambdaQueryWrapper 的条件构造
userMapper.selectOne(new LambdaQueryWrapper<User>()
    .eq(User::getUsername, "admin")       // User::getUsername 是方法引用
    .eq(User::getStatus, 1)
);

// Stream 操作
List<NewsVO> voList = newsList.stream()
    .map(n -> toVO(n))                   // n -> toVO(n) 是 Lambda
    .filter(vo -> vo != null)            // 过滤空值
    .collect(Collectors.toList());       // 收集成 List
```

---

## 5. Stream API

### 是什么
一种函数式操作集合的方式，链式调用，像流水线一样处理数据。

### 核心操作

```java
List<String> names = users.stream()
    .filter(u -> u.getStatus() == 1)     // 过滤：只要启用的用户
    .map(u -> u.getUsername())           // 转换：User → String
    .sorted()                            // 排序
    .limit(10)                           // 取前 10 个
    .collect(Collectors.toList());       // 收集成 List

// 统计
long count = users.stream().count();
boolean allActive = users.stream().allMatch(u -> u.getStatus() == 1);

// 转 Map
Map<Long, String> userMap = users.stream()
    .collect(Collectors.toMap(User::getUserId, User::getUsername));
```

### 项目中哪里用到了
NewsService.java 中大量使用 Stream 做数据转换：
```java
Map<Long, String> userMap = userMapper.selectList(null).stream()
    .collect(Collectors.toMap(User::getUserId, User::getUsername));
// 把 List<User> 转成 Map<userId, username>，后面 O(1) 查找
```

---

## 6. Optional

### 是什么
一个容器，可能包含值也可能为空，强迫你处理空值情况。

### 项目中哪里用到了
```java
// ServletUtils.java
public static Optional<HttpServletRequest> getCurrentRequest() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return Optional.ofNullable(attrs).map(ServletRequestAttributes::getRequest);
}
// 调用方必须处理可能为空的情况
```

---

## 7. 访问修饰符

| 修饰符 | 本类 | 同包 | 子类 | 任何地方 |
|--------|------|------|------|---------|
| private | ✅ | ❌ | ❌ | ❌ |
| default(不写) | ✅ | ✅ | ❌ | ❌ |
| protected | ✅ | ✅ | ✅ | ❌ |
| public | ✅ | ✅ | ✅ | ✅ |

面试时回答"封装"相关问题用：private 字段 + public getter/setter = 控制访问权限。

---

## 8. static vs 实例

| | static | 非 static（实例） |
|------|--------|------|
| 属于谁 | 类本身 | 类的每个对象 |
| 怎么调用 | `类名.方法()` | `对象.方法()` |
| 内存 | 只有一份 | 每个对象各一份 |

```java
public class Result<T> {
    // 静态方法：不需要 new Result() 就能用
    public static <T> Result<T> ok(T data) { ... }

    // 实例方法：需要先 new Result() 才能用
    public int getCode() { return this.code; }
}

// 使用
Result<User> r = Result.ok(user);   // 静态方法直接用类名调
int code = r.getCode();             // 实例方法用对象调
```

---

## 9. final 关键字

| 用法 | 含义 |
|------|------|
| `final class X` | 这个类不能被继承 |
| `final void method()` | 这个方法不能被子类重写 |
| `final int x = 5` | 这个变量只能赋值一次（常量） |
| `final User user = ...` | 引用不能变（不能指向别的对象），但对象内部可以变 |

项目中大量使用 `private final` 声明依赖：
```java
private final UserService userService;  // 构造函数赋值后不可变
```

---

## 10. this 关键字

- `this.name` → 当前对象的 name 字段（区别于参数 name）
- `this()` → 调用本类的另一个构造方法
- `this.getById()` → 调用自己的方法
- 在构造方法中：`this.name = name;`（区分字段和参数）

---

> **学完这篇，你就能看懂项目中 80% 的 Java 代码了。剩下的 20% 是框架特有的写法，在下一篇讲。**
