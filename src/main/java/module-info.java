module com.kineticfitness {
    requires javafx.controls;
    requires java.sql;

    exports com.kineticfitness;
    exports com.kineticfitness.model;
    exports com.kineticfitness.view;

    opens com.kineticfitness.db;   // ← lets JUnit reflectively run the DAO tests
}