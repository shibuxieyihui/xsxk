import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程列表后端接口，支持分页、已发布可选课程过滤、实时刷新。
 */
@RestController
@RequestMapping("/api/student/courses")
public class StudentViewCourseListController {

    private final CourseService courseService;

    @Autowired
    public StudentViewCourseListController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResult<CourseDTO>> listCourses(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }

        PageResult<CourseDTO> result = courseService.getPublishedOptionalCourses(page, size);
        return ResponseEntity.ok(result);
    }
}

@Service
class CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public PageResult<CourseDTO> getPublishedOptionalCourses(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Course> coursePage = courseRepository.findByPublishedTrueAndOptionalCourseTrue(pageable);

        List<CourseDTO> courseList = coursePage.stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());

        String message = courseList.isEmpty() ? "暂无可选课程" : "";

        return new PageResult<>(courseList,
                coursePage.getTotalElements(),
                coursePage.getTotalPages(),
                page,
                size,
                message);
    }
}

@Repository
interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByPublishedTrueAndOptionalCourseTrue(Pageable pageable);
}

@Entity
class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private String teacherName;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private String schedule;

    @Column(nullable = false)
    private Integer maxEnrollment;

    @Column(nullable = false)
    private Integer enrolledCount;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean optionalCourse;

    public Long getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Integer getCredits() {
        return credits;
    }

    public String getSchedule() {
        return schedule;
    }

    public Integer getMaxEnrollment() {
        return maxEnrollment;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isOptionalCourse() {
        return optionalCourse;
    }
}

class CourseDTO {
    private Long id;
    private String courseName;
    private String teacherName;
    private Integer credits;
    private String schedule;
    private Integer maxEnrollment;
    private Integer enrolledCount;
    private Integer availableSeats;

    public static CourseDTO fromEntity(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.id = course.getId();
        dto.courseName = course.getCourseName();
        dto.teacherName = course.getTeacherName();
        dto.credits = course.getCredits();
        dto.schedule = course.getSchedule();
        dto.maxEnrollment = course.getMaxEnrollment();
        dto.enrolledCount = course.getEnrolledCount();
        dto.availableSeats = Math.max(0, course.getMaxEnrollment() - course.getEnrolledCount());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Integer getCredits() {
        return credits;
    }

    public String getSchedule() {
        return schedule;
    }

    public Integer getMaxEnrollment() {
        return maxEnrollment;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }
}

class PageResult<T> {
    private List<T> data;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private String message;

    public PageResult(List<T> data, long totalElements, int totalPages, int page, int size, String message) {
        this.data = data;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
        this.message = message;
    }

    public List<T> getData() {
        return data;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getMessage() {
        return message;
    }
}
