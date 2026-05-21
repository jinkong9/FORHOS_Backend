package com.jin.practice.reception.entity;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReceptionSchemaTest {

    @Test
    void receptionQueueNumberIsUniquePerHospitalAndDate() {
        Table table = Reception.class.getAnnotation(Table.class);

        assertThat(table).isNotNull();
        assertThat(table.uniqueConstraints())
                .anySatisfy(uniqueConstraint -> assertThat(Set.of(uniqueConstraint.columnNames()))
                        .containsExactlyInAnyOrder("hospital_id", "queue_date", "queue_number"));
    }
}
