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
    private String address;
    private String email;
    private Date bornDate;
    private String bornAt;
    private Date registerAt;
}
