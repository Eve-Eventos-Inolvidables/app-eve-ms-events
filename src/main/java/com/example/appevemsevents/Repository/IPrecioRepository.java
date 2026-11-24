package com.example.appevemsevents.Repository;

import com.example.appevecommon.Models.Event.Batch.SectorByBatch;
import com.example.appevecommon.Repository.IBaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IPrecioRepository extends IBaseRepository<SectorByBatch> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE sectors_by_batch
               SET quantity = quantity - :amount
             WHERE id = :id
               AND quantity >= :amount
               AND is_archived = false
            """, nativeQuery = true)
    int decrementQuantityIfAvailable(
            @Param("id") Long id,
            @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE sectors_by_batch
               SET quantity = quantity + :amount
             WHERE id = :id
               AND is_archived = false
            """, nativeQuery = true)
    int restoreQuantity(
            @Param("id") Long id,
            @Param("amount") int amount);
}
