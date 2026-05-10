package com.quang.marketplace.modules.seller.infrastructure;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SellerProfileRepositoryIT {

    @Autowired
    SellerProfileRepository repo;

    @Test
    public void uniqueUserId_constraint_prevents_duplicate_profiles() {
        SellerProfile a = new SellerProfile(100L, "Shop A");
        a.activate();
        repo.saveAndFlush(a);

        SellerProfile b = new SellerProfile(100L, "Shop B");
        b.activate();

        assertThrows(DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(b);
        });
    }
}
