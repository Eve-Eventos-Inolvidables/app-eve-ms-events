package com.example.appevemsevents.Repository;

import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevecommon.Repository.IBaseRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface IEventRepository extends IBaseRepository<SimpleEvent>{
}
