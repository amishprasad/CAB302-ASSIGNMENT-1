module com.kineticfitness {
    requires javafx.controls;
    requires java.sql;

    exports com.kineticfitness;
    exports com.kineticfitness.model;
    exports com.kineticfitness.view;
    exports com.kineticfitness.service;

    opens com.kineticfitness.db;      // lets JUnit reflectively run the DAO tests
    opens com.kineticfitness.model;   // model tests
    opens com.kineticfitness.service; // service tests
    opens com.kineticfitness.util;    // BmiCalculator / WorkoutStats tests
}