package com.zhixu.studycompass.service;

import com.zhixu.studycompass.dto.StudyRecordRequest;
import com.zhixu.studycompass.model.StudyRecord;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordService {
    List<StudyRecord> findByUserId(Long userId, LocalDate startDate, LocalDate endDate);
    StudyRecord findById(Long id);
    StudyRecord create(StudyRecordRequest request);
    StudyRecord update(Long id, StudyRecordRequest request);
    void delete(Long id);
}
