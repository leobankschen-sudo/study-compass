package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.domain.AiReportType;
import com.zhixu.studycompass.model.AiReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface AiReportMapper {
    List<AiReport> findByUserId(@Param("userId") Long userId, @Param("type") AiReportType type);
    Optional<AiReport> findById(Long id);
    int insert(AiReport report);
    int deleteById(Long id);
}

