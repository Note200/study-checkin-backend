package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.Classes;
import com.studycheckin.backend.mapper.ClassesMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClassesService extends ServiceImpl<ClassesMapper, Classes> {

    public Classes createClass(String name, Long creatorId) {
        Classes c = new Classes();
        c.setName(name);
        c.setCreatorId(creatorId);
        c.setInviteCode(generateCode());
        save(c);
        return c;
    }

    public Classes getByInviteCode(String code) {
        return lambdaQuery().eq(Classes::getInviteCode, code).one();
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
