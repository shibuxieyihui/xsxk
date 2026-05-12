/**
 * 学生认证模块 - 登录认证核心业务
 * 功能：学生登录、密码校验、账号异常检测、安全认证
 */
@Service
public class StudentAuthService {

    @Autowired
    private StudentMapper studentMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 学生登录认证
     */
    public String studentLogin(String studentNo, String password) {
        // 1. 查询学生信息
        Student student = studentMapper.selectByStudentNo(studentNo);
        if (student == null) {
            throw new RuntimeException("账号不存在");
        }

        // 2. 账号异常检测（安全管理）
        if (student.getErrorCount() >= 5) {
            throw new RuntimeException("账号已锁定，多次密码错误");
        }
        if (student.getStatus() == 2) {
            throw new RuntimeException("账号已被禁用");
        }

        // 3. 密码校验
        if (!encoder.matches(password, student.getPassword())) {
            student.setErrorCount(student.getErrorCount() + 1);
            studentMapper.updateById(student);
            throw new RuntimeException("密码错误");
        }

        // 4. 登录成功，重置状态
        student.setErrorCount(0);
        student.setLastLoginTime(LocalDateTime.now());
        studentMapper.updateById(student);

        return "登录成功，认证通过";
    }
}