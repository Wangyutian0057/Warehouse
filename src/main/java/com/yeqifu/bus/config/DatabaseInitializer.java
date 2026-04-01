package com.yeqifu.bus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Database Schema Check...");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if EXPECTED_LOCATION column exists in BUS_CHECK_ASSET
            boolean hasExpectedLoc = false;
            boolean hasActualLoc = false;
            
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "BUS_CHECK_ASSET", "EXPECTED_LOCATION")) {
                if (rs.next()) hasExpectedLoc = true;
            }
            // Try lowercase if uppercase fails (Oracle stores metadata in uppercase usually, but checking both is safer)
            if (!hasExpectedLoc) {
                 try (ResultSet rs = conn.getMetaData().getColumns(null, null, "bus_check_asset", "expected_location")) {
                    if (rs.next()) hasExpectedLoc = true;
                }
            }

            if (!hasExpectedLoc) {
                System.out.println("Adding EXPECTED_LOCATION column to BUS_CHECK_ASSET...");
                try {
                    stmt.execute("ALTER TABLE bus_check_asset ADD (expected_location VARCHAR2(255))");
                    System.out.println("Added EXPECTED_LOCATION successfully.");
                } catch (Exception e) {
                    System.err.println("Failed to add EXPECTED_LOCATION: " + e.getMessage());
                }
            }

            // Check ACTUAL_LOCATION
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "BUS_CHECK_ASSET", "ACTUAL_LOCATION")) {
                if (rs.next()) hasActualLoc = true;
            }
             if (!hasActualLoc) {
                 try (ResultSet rs = conn.getMetaData().getColumns(null, null, "bus_check_asset", "actual_location")) {
                    if (rs.next()) hasActualLoc = true;
                }
            }

            if (!hasActualLoc) {
                System.out.println("Adding ACTUAL_LOCATION column to BUS_CHECK_ASSET...");
                try {
                    stmt.execute("ALTER TABLE bus_check_asset ADD (actual_location VARCHAR2(255))");
                    System.out.println("Added ACTUAL_LOCATION successfully.");
                } catch (Exception e) {
                    System.err.println("Failed to add ACTUAL_LOCATION: " + e.getMessage());
                }
            }
            
            // Backfill data for existing records if new columns were added or are empty
            if (!hasExpectedLoc) { // Only run if we just added the column, or we can run it safely anyway
                 System.out.println("Backfilling EXPECTED_LOCATION from current Asset location...");
                 try {
                     stmt.execute("UPDATE bus_check_asset ca SET ca.expected_location = (SELECT a.location FROM bus_asset a WHERE a.id = ca.asset_id) WHERE ca.expected_location IS NULL");
                     // Also init actual_location as expected_location for old records (assuming they matched at that time or just default)
                     stmt.execute("UPDATE bus_check_asset ca SET ca.actual_location = ca.expected_location WHERE ca.actual_location IS NULL");
                     System.out.println("Backfill completed.");
                 } catch (Exception e) {
                     System.err.println("Failed to backfill data: " + e.getMessage());
                 }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Database Schema Check Completed.");
    }
}
