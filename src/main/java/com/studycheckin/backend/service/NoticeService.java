package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.Notice;
import com.studycheckin.backend.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeService extends ServiceImpl<NoticeMapper, Notice> {

    public List<Notice> listAll() {
        return lambdaQuery().orderByDesc(Notice::getCreateTime).list();
    }

    public void publish(Notice notice, Long adminId) {
        notice.setAdminId(adminId);
        save(notice);
    }

    public void deleteNotice(Long id) {
        removeById(id);
    }
}
