package com.quang.marketplace.modules.catalog.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.quang.marketplace.shared.error.BusinessRuleException;
import com.quang.marketplace.shared.error.ValidationException;

public class ProductVariantInventoryTest {

    @Test
    void newInventory_rejectsNegativeOnHandQuantity() {
        assertThrows(
            ValidationException.class,
            () -> new ProductVariantInventory(-1)
        );
    }

    @Test
    void newInventory_withValidInputs_storesInitialQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertEquals(10, inventory.getOnHandQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(10, inventory.getAvailableQuantity());
    }

    @Test
    void hasAvailableQuantity_returnsTrueWhenEnoughAvailable() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertTrue(inventory.hasAvailableQuantity(5));
    }

    @Test
    void hasAvailableQuantity_returnsFalseWhenNotEnoughAvailable() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertFalse(inventory.hasAvailableQuantity(11));
    }

    @Test
    void hasAvailableQuantity_returnsFalseForZeroOrNegativeQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertFalse(inventory.hasAvailableQuantity(0));
        assertFalse(inventory.hasAvailableQuantity(-1));
    }

    @Test
    void adjustOnHandQuantity_increasesOnHandQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        inventory.adjustOnHandQuantity(5);

        assertEquals(15, inventory.getOnHandQuantity());
        assertEquals(15, inventory.getAvailableQuantity());
    }

    @Test
    void adjustOnHandQuantity_decreasesOnHandQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        inventory.adjustOnHandQuantity(-3);

        assertEquals(7, inventory.getOnHandQuantity());
        assertEquals(7, inventory.getAvailableQuantity());
    }

    @Test
    void adjustOnHandQuantity_rejectsWhenOnHandWouldDropBelowZero() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            BusinessRuleException.class,
            () -> inventory.adjustOnHandQuantity(-11)
        );
    }

    @Test
    void adjustOnHandQuantity_rejectsWhenOnHandWouldDropBelowReserved() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);
        inventory.reserve(7);

        assertThrows(
            BusinessRuleException.class,
            () -> inventory.adjustOnHandQuantity(-4)
        );
    }

    @Test
    void reserve_rejectsZeroQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            ValidationException.class,
            () -> inventory.reserve(0)
        );
    }

    @Test
    void reserve_rejectsNegativeQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            ValidationException.class,
            () -> inventory.reserve(-1)
        );
    }

    @Test
    void reserve_rejectsWhenInsufficientAvailableInventory() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            BusinessRuleException.class,
            () -> inventory.reserve(11)
        );
    }

    @Test
    void reserve_validQuantity_increasesReservedAndReducesAvailable() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        inventory.reserve(4);

        assertEquals(10, inventory.getOnHandQuantity());
        assertEquals(4, inventory.getReservedQuantity());
        assertEquals(6, inventory.getAvailableQuantity());
    }

    @Test
    void releaseReservation_rejectsZeroQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            ValidationException.class,
            () -> inventory.releaseReservation(0)
        );
    }

    @Test
    void releaseReservation_rejectsMoreThanReserved() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);
        inventory.reserve(3);

        assertThrows(
            BusinessRuleException.class,
            () -> inventory.releaseReservation(4)
        );
    }

    @Test
    void releaseReservation_validQuantity_decreasesReservedAndRestoresAvailable() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);
        inventory.reserve(6);

        inventory.releaseReservation(2);

        assertEquals(10, inventory.getOnHandQuantity());
        assertEquals(4, inventory.getReservedQuantity());
        assertEquals(6, inventory.getAvailableQuantity());
    }

    @Test
    void commitReservation_rejectsZeroQuantity() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            ValidationException.class,
            () -> inventory.commitReservation(0)
        );
    }

    @Test
    void commitReservation_rejectsMoreThanReserved() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);
        inventory.reserve(3);

        assertThrows(
            BusinessRuleException.class,
            () -> inventory.commitReservation(4)
        );
    }

    @Test
    void commitReservation_validQuantity_decreasesReservedAndOnHand() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);
        inventory.reserve(6);

        inventory.commitReservation(4);

        assertEquals(6, inventory.getOnHandQuantity());
        assertEquals(2, inventory.getReservedQuantity());
        assertEquals(4, inventory.getAvailableQuantity());
    }

    @Test
    void updateReorderThreshold_rejectsNegativeThreshold() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        assertThrows(
            ValidationException.class,
            () -> inventory.updateReorderThreshold(-1)
        );
    }

    @Test
    void updateReorderThreshold_validThreshold_updatesThreshold() {
        ProductVariantInventory inventory = new ProductVariantInventory( 10);

        inventory.updateReorderThreshold(5);

        assertEquals(5, inventory.getReorderThreshold());
    }
}