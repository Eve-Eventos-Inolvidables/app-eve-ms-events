package com.example.appevemsevents.Service.EventSimple;

import com.example.appevecommon.Models.Event.SimpleEvent;
import com.example.appevecommon.Repository.IBaseRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class EventSimpleServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void filtersByInclusiveStartDateAndExclusiveBeforeDate() {
        IBaseRepository<SimpleEvent> repository = mock(IBaseRepository.class);
        EventSimpleMapper mapper = mock(EventSimpleMapper.class);
        CacheManager cacheManager = mock(CacheManager.class);
        EventSimpleService service =
                new EventSimpleService(repository, mapper, cacheManager);

        LocalDate dateFrom = LocalDate.of(2026, 11, 24);
        LocalDate dateBefore = LocalDate.of(2026, 11, 25);
        EventSimpleFilter filter = new EventSimpleFilter();
        filter.setDateFrom(dateFrom);
        filter.setDateBefore(dateBefore);

        Root<SimpleEvent> root = mock(Root.class);
        Path<LocalDate> eventDate = mock(Path.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate fromPredicate = mock(Predicate.class);
        Predicate beforePredicate = mock(Predicate.class);

        when(root.<LocalDate>get("date")).thenReturn(eventDate);
        when(criteriaBuilder.greaterThanOrEqualTo(eventDate, dateFrom))
                .thenReturn(fromPredicate);
        when(criteriaBuilder.lessThan(eventDate, dateBefore))
                .thenReturn(beforePredicate);

        service.toSpecification(filter).toPredicate(root, null, criteriaBuilder);

        verify(criteriaBuilder).greaterThanOrEqualTo(eventDate, dateFrom);
        verify(criteriaBuilder).lessThan(eventDate, dateBefore);
        verify(criteriaBuilder).and(any(Predicate[].class));
    }
}
