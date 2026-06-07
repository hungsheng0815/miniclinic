package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, String> {

    // 不用寫實作，Spring 會依命名規則自動產生
    List<Doctor> findByDepartment(String department);

    // 命名規則查詢只能查欄位值，無法表達 DISTINCT 聚合
    // 要取所有不重複的科別清單，必須用 @Query 寫 JPQL
    @Query("SELECT DISTINCT d.department FROM Doctor d ORDER BY d.department")
    List<String> findAllDepartments();
}