package org.walterinkitchen.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collation = Person.COLLECTION)
public class Person {
    public static final String COLLECTION = "person";

    @Id
    private String id;
    private String firstName;
    private String secondName;
    private int age;
    private Title title;
    private String city;
    private String address;
    private String email;
    private Date bornDate;
    private String bornAt;
    private Date registerAt;
    private Double salary;
    private int bonusRate;
    private Double income;
    private Boolean advance;

    public enum Title {
        BOSS,
        SUPERVISOR,
        ENGINEER;
    }
}
