package com.yooyoung.clotheser.admin.repository;

import com.yooyoung.clotheser.admin.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByOrderByIdDesc();

}
