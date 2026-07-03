package com.example.news.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.modules.category.entity.Category;
import com.example.news.modules.category.mapper.CategoryMapper;
import com.example.news.modules.news.entity.News;
import com.example.news.modules.news.mapper.NewsMapper;
import com.example.news.modules.review.entity.Review;
import com.example.news.modules.review.mapper.ReviewMapper;
import com.example.news.modules.slide.entity.Slide;
import com.example.news.modules.slide.mapper.SlideMapper;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首次启动自动初始化基础数据（等价于原 PHP 的 auto-install）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final NewsMapper newsMapper;
    private final SlideMapper slideMapper;
    private final ReviewMapper reviewMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initCategories();
        initSlides();
        initNews();
        initReviews();
        log.info("✅ 数据初始化检查完成");
    }

    private void initUsers() {
        if (userMapper.selectCount(null) > 0) return;
        log.info("⏳ 初始化用户数据...");

        String pw = passwordEncoder.encode("admin123");
        String epw = passwordEncoder.encode("editor123");
        String apw = passwordEncoder.encode("auditor123");
        String upw = passwordEncoder.encode("user123");

        List<User> users = List.of(
            createUser("admin", pw, "admin"),
            createUser("admin2", pw, "admin"),
            createUser("auditor", apw, "auditor"),
            createUser("方瑜", apw, "auditor"),
            createUser("editor", epw, "editor"),
            createUser("林澈", epw, "editor"),
            createUser("宋禾", epw, "editor"),
            createUser("邵宁", epw, "editor"),
            createUser("江野", epw, "editor"),
            createUser("许念", epw, "editor"),
            createUser("何煦", epw, "editor"),
            createUser("陈知远", epw, "editor"),
            createUser("user", upw, "user"),
            createUser("读者小王", upw, "user")
        );
        users.forEach(userMapper::insert);
        log.info("  ✅ 创建 {} 个用户", users.size());
    }

    private void initCategories() {
        if (categoryMapper.selectCount(null) > 0) return;
        log.info("⏳ 初始化分类数据...");
        String[] names = {"体育", "财经", "科技", "文化", "时政", "教育", "民生"};
        for (int i = 0; i < names.length; i++) {
            Category c = new Category();
            c.setName(names[i]);
            c.setSortOrder(i + 1);
            categoryMapper.insert(c);
        }
        log.info("  ✅ 创建 {} 个分类", names.length);
    }

    private void initSlides() {
        if (slideMapper.selectCount(null) > 0) return;
        log.info("⏳ 初始化轮播数据...");
        String[][] data = {
            {"一带一路数字经济论坛", "4_assets/images/slide1.jpg", "1"},
            {"AI医疗新时代", "4_assets/images/slide_ai.jpg", "2"},
            {"文化与旅游融合", "4_assets/images/slide_culture.jpg", "3"},
        };
        for (String[] d : data) {
            Slide s = new Slide();
            s.setTitle(d[0]);
            s.setImage(d[1]);
            s.setSortOrder(Integer.parseInt(d[2]));
            s.setStatus(1);
            slideMapper.insert(s);
        }
        log.info("  ✅ 创建 {} 个轮播", data.length);
    }

    private void initNews() {
        if (newsMapper.selectCount(null) > 0) return;
        log.info("⏳ 初始化种子新闻...");

        // 查找编辑和分类用于创建种子新闻
        User editor = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "林澈"));
        User songhe = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "宋禾"));
        User heXu = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "何煦"));
        User jiangYe = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "江野"));
        User xuNian = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "许念"));
        User shaoNing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "邵宁"));
        User editorUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "editor"));

        Category shizheng = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "时政"));
        Category caijing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "财经"));
        Category jiaoyu = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "教育"));
        Category keji = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "科技"));
        Category wenhua = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "文化"));
        Category minsheng = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "民生"));
        Category tiyu = categoryMapper.selectOne(new LambdaQueryWrapper<Category>().eq(Category::getName, "体育"));

        if (editor == null || shizheng == null) return;

        // Published news — 置顶
        insertNews(editor.getUserId(), shizheng.getCategoryId(), "一带一路数字经济合作论坛在京举行",
            "来自四十余个国家和地区的代表围绕数字基础设施互联互通、跨境电商和人工智能治理达成多项共识。论坛发布了《数字丝绸之路北京倡议》。",
            "published", null, 1, 35620, "2026-06-26 18:30:00");

        insertNews(songhe.getUserId(), caijing.getCategoryId(), "数字人民币试点覆盖全国主要商圈",
            "数字人民币受理环境已覆盖全国超过200个主要商圈，日均交易笔数突破500万。",
            "published", null, 1, 32100, "2026-06-28 21:15:00");

        insertNews(heXu.getUserId(), jiaoyu.getCategoryId(), "教育部发布新时代教师队伍建设意见",
            "意见提出到2030年建成高素质专业化教师队伍，重点加强乡村教师待遇保障和职业发展通道。",
            "published", null, 1, 28700, "2026-06-27 16:45:00");

        // Published news — 非置顶
        insertNews(jiangYe.getUserId(), keji.getCategoryId(), "量子计算云平台向公众免费开放体验",
            "国内首个面向公众的量子计算云平台正式上线，用户可通过浏览器访问并运行简单的量子算法。",
            "published", null, 0, 27800, "2026-06-27 10:30:00");

        insertNews(editor.getUserId(), shizheng.getCategoryId(), "城市更新示范区发布数字治理新方案",
            "围绕交通疏导、民生服务和社区治理，示范区上线多部门联动的数字化协同平台。",
            "published", null, 0, 24860, "2026-06-30 20:18:00");

        insertNews(jiangYe.getUserId(), keji.getCategoryId(), "人工智能辅助诊断系统覆盖基层医院",
            "AI辅助诊断系统已在超过800家基层医疗机构部署，覆盖胸部影像、眼底筛查和心电分析等场景。",
            "published", null, 0, 22300, "2026-06-28 14:20:00");

        insertNews(editor.getUserId(), shizheng.getCategoryId(), "全国统一大市场建设加速推进",
            "国务院常务会议审议通过统一大市场建设年度评估报告，市场准入负面清单事项再缩减12%。",
            "published", null, 0, 19800, "2026-06-30 08:15:00");

        insertNews(jiangYe.getUserId(), keji.getCategoryId(), "5G+工业互联网融合应用示范项目启动",
            "工信部公布第三批5G+工业互联网示范项目名单，涵盖汽车制造、钢铁冶炼和家电装配等行业。",
            "published", null, 0, 19500, "2026-06-30 16:40:00");

        insertNews(xuNian.getUserId(), wenhua.getCategoryId(), "本地文旅市集夜间客流创季度新高",
            "沉浸式演出、非遗摊位和社区品牌联动，成为夜间消费增长的重要因素。",
            "published", null, 0, 18630, "2026-06-29 22:10:00");

        insertNews(heXu.getUserId(), jiaoyu.getCategoryId(), "高校毕业生就业服务季活动全面启动",
            "人社部和教育部联合启动2026届高校毕业生就业服务季，线上线下同步举办千余场招聘活动。",
            "published", null, 0, 15600, "2026-06-30 11:25:00");

        insertNews(shaoNing.getUserId(), minsheng.getCategoryId(), "社区养老服务体系建设取得新进展",
            "全国已建成社区养老服务站超过12万个，覆盖城市社区的86%和农村行政村的41%。",
            "published", null, 0, 12400, "2026-06-29 15:50:00");

        insertNews(songhe.getUserId(), caijing.getCategoryId(), "新能源汽车下乡活动带动乡村绿色出行",
            "今年上半年新能源汽车下乡车型销量同比增长62%，充电桩进村覆盖率提升至58%。",
            "published", null, 0, 11200, "2026-06-30 14:10:00");

        insertNews(shaoNing.getUserId(), minsheng.getCategoryId(), "多地推出住房租赁新政保障新市民安居",
            "十余个重点城市出台保障性租赁住房新政，降低新市民和青年人的申请门槛。",
            "published", null, 0, 9800, "2026-06-29 19:30:00");

        insertNews(xuNian.getUserId(), wenhua.getCategoryId(), "全国非遗保护传承成果展在杭州开幕",
            "展览集中展示了近五年来全国非物质文化遗产保护传承的代表性成果和数字化保护技术应用。",
            "published", null, 0, 8900, "2026-06-30 10:50:00");

        insertNews(xuNian.getUserId(), wenhua.getCategoryId(), "大运河文化带建设取得阶段性成果",
            "大运河沿线八个省市完成文化遗产普查，沿线步道和绿道贯通率超过70%。",
            "published", null, 0, 7600, "2026-06-28 17:20:00");

        // Pending news
        insertNews(heXu.getUserId(), jiaoyu.getCategoryId(), "高校实验室开放日聚焦智能媒体技术",
            "开放日设置新闻自动化写作、数据可视化和虚拟演播三类体验模块。",
            "pending", null, 0, 0, "2026-07-01 09:48:00");

        insertNews(shaoNing.getUserId(), minsheng.getCategoryId(), "社区健康服务站上线预约分诊系统",
            "居民可按症状、时段和家庭医生进行预约，系统自动提醒就诊材料并估算等待时间。",
            "pending", null, 0, 0, "2026-07-01 11:12:00");

        insertNews(songhe.getUserId(), caijing.getCategoryId(), "数字农业示范基地带动乡村振兴新路径",
            "全国已建成120个数字农业示范基地，涵盖精准种植、智能灌溉和农产品溯源等应用。",
            "pending", null, 0, 0, "2026-07-01 08:35:00");

        insertNews(jiangYe.getUserId(), keji.getCategoryId(), "智慧交通管理系统在二线城市推广试点",
            "基于AI视觉与车联网技术的智慧交通系统在六个二线城市启动试点。",
            "pending", null, 0, 0, "2026-07-01 10:05:00");

        insertNews(heXu.getUserId(), jiaoyu.getCategoryId(), "中小学课后服务提质增效工作方案出台",
            "方案要求各地丰富课后服务内容供给，引入科技、艺术和体育类校外资源参与。",
            "pending", null, 0, 0, "2026-06-30 17:20:00");

        insertNews(songhe.getUserId(), caijing.getCategoryId(), "跨境旅游复苏带动边贸小镇经济升温",
            "上半年跨境旅游人次恢复至疫情前水平，多个边境特色小镇的住宿和零售业收入同比增长45%以上。",
            "pending", null, 0, 0, "2026-06-30 14:50:00");

        // Drafts
        insertNews(jiangYe.getUserId(), keji.getCategoryId(), "新一代工业传感器进入规模化试产",
            "企业宣布完成低功耗传感器试产线调试，将用于设备状态监测与预测性维护。",
            "draft", null, 0, 0, "2026-07-01 10:22:00");

        insertNews(editor.getUserId(), shizheng.getCategoryId(), "跨境数据流动安全管理办法征求意见",
            "网信办就数据出境安全评估办法公开征求意见。",
            "draft", null, 0, 0, "2026-07-01 09:15:00");

        insertNews(shaoNing.getUserId(), minsheng.getCategoryId(), "全民健身设施补短板工程年度总结",
            "本年度新建或改扩建社区健身中心2800余个，新增健身步道超5000公里。",
            "draft", null, 0, 0, "2026-07-01 14:30:00");

        insertNews(xuNian.getUserId(), wenhua.getCategoryId(), "传统戏曲进校园活动覆盖百所中小学",
            "活动以京剧、昆曲和地方戏种为主，结合数字多媒体手段。",
            "draft", null, 0, 0, "2026-07-01 11:05:00");

        insertNews(songhe.getUserId(), caijing.getCategoryId(), "绿色金融支持低碳技术创新政策解读",
            "央行联合多部门发布绿色金融支持低碳技术目录，覆盖储能、氢能和碳捕集等领域。",
            "draft", null, 0, 0, "2026-07-01 15:40:00");

        // Rejected
        insertNews(songhe.getUserId(), caijing.getCategoryId(), "区域金融服务平台新增小微企业信用模型",
            "模型将纳税、经营流水和履约记录纳入综合评价。",
            "rejected", "需补充模型数据来源和风险控制说明，避免表述过度承诺", 0, 0, "2026-06-28 11:06:00");

        insertNews(shaoNing.getUserId(), minsheng.getCategoryId(), "城市垃圾分类智能化管理成效评估",
            "报告称智能分类设备使居民投放准确率提升至82%。",
            "rejected", "建议增加不同城市之间对比数据，并补充居民满意度调查结果", 0, 0, "2026-06-28 16:40:00");

        insertNews(heXu.getUserId(), jiaoyu.getCategoryId(), "在线教育平台内容审核标准引发讨论",
            "多家在线教育平台呼吁建立统一的课程内容审核标准。",
            "rejected", "需采访相关监管部门意见，呈现多方观点", 0, 0, "2026-06-30 09:25:00");

        if (editorUser != null && tiyu != null) {
            insertNews(editorUser.getUserId(), tiyu.getCategoryId(), "普通高校体育特长生招生政策调整设想",
                "部分高校表示将调整体育特长生招生比例。",
                "rejected", "稿件数据支撑不足，建议引用官方统计数据后重新提交", 0, 0, "2026-06-27 15:00:00");
        }

        log.info("  ✅ 创建种子新闻");
    }

    private void initReviews() {
        if (reviewMapper.selectCount(null) > 0) return;
        log.info("⏳ 初始化种子评论...");

        News beltAndRoad = newsMapper.selectOne(new LambdaQueryWrapper<News>().eq(News::getTitle, "一带一路数字经济合作论坛在京举行"));
        News digitalYuan = newsMapper.selectOne(new LambdaQueryWrapper<News>().eq(News::getTitle, "数字人民币试点覆盖全国主要商圈"));
        News teachers = newsMapper.selectOne(new LambdaQueryWrapper<News>().eq(News::getTitle, "教育部发布新时代教师队伍建设意见"));
        News quantum = newsMapper.selectOne(new LambdaQueryWrapper<News>().eq(News::getTitle, "量子计算云平台向公众免费开放体验"));
        News aiMedical = newsMapper.selectOne(new LambdaQueryWrapper<News>().eq(News::getTitle, "人工智能辅助诊断系统覆盖基层医院"));

        if (beltAndRoad != null) {
            insertReview(beltAndRoad.getNewsId(), "财经观察者", "跨境电商方面的具体合作项目预计何时落地？", "approved");
            insertReview(beltAndRoad.getNewsId(), "匿名用户", "出售低价数码产品，加V信xxxxx", "pending");
        }
        if (digitalYuan != null) {
            insertReview(digitalYuan.getNewsId(), "数码爱好者", "数字人民币和现有移动支付工具的主要区别是什么？", "pending");
            insertReview(digitalYuan.getNewsId(), "热心网友", "这个政策方向很好，希望能尽快落实到位！", "approved");
        }
        if (teachers != null) {
            insertReview(teachers.getNewsId(), "一线教师", "希望乡村教师待遇能真正提升，尤其是偏远地区的编制和职称问题。", "pending");
        }
        if (quantum != null) {
            insertReview(quantum.getNewsId(), "物理系学生", "已经注册体验了量子编程课程，入门门槛比想象的低，推荐！", "approved");
        }
        if (aiMedical != null) {
            insertReview(aiMedical.getNewsId(), "基层医生张", "我们医院刚上线这套AI系统，确实能帮上忙，但需要持续优化。", "approved");
        }
        log.info("  ✅ 创建种子评论");
    }

    // ===== Helper methods =====
    private User createUser(String username, String password, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        u.setStatus(1);
        return u;
    }

    private void insertNews(Long userId, Long categoryId, String title, String content,
                            String status, String rejectReason, int isTop, int clicked, String createdAt) {
        News n = new News();
        n.setUserId(userId);
        n.setCategoryId(categoryId);
        n.setTitle(title);
        n.setContent(content);
        n.setStatus(status);
        n.setRejectReason(rejectReason);
        n.setIsTop(isTop);
        n.setClicked((long) clicked);
        n.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        n.setUpdatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        newsMapper.insert(n);
    }

    private void insertReview(Long newsId, String username, String content, String state) {
        Review r = new Review();
        r.setNewsId(newsId);
        r.setUsername(username);
        r.setContent(content);
        r.setState(state);
        r.setIp("127.0.0.1");
        reviewMapper.insert(r);
    }
}
