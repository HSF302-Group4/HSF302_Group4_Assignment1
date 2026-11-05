package com.fu.cinema.desktop.model;

import com.fu.cinema.entity.Customer;

import java.time.LocalDate;
import java.time.Period;

public record CustomerItem(Long id, String fullName, String mobile, int age) {

    public static CustomerItem from(Customer c) {
        int calculatedAge = 0;
        if (c.getBirthday() != null) {
            calculatedAge = Period.between(c.getBirthday(), LocalDate.now()).getYears();
        }
        return new CustomerItem(c.getCustomerId(), c.getFullName(), c.getMobile(), calculatedAge);
    }

    @Override
    public String toString() {
        return fullName + " – " + mobile + " – " + age + " tuổi";
    }
}
