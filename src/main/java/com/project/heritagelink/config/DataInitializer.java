package com.project.heritagelink.config;

import com.project.heritagelink.model.Claim;
import com.project.heritagelink.model.Claimant;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.ClaimStatus;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import com.project.heritagelink.repository.ClaimRepository;
import com.project.heritagelink.repository.ClaimantRepository;
import com.project.heritagelink.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Populates the in-memory H2 database with realistic mock data for demonstrating and testing.
 * Runs once on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;
    private final ClaimantRepository claimantRepository;
    private final ClaimRepository claimRepository;

    @Override
    public void run(String... args) {
        log.info("=== Heritage Link: Loading mock data ===");
        seedClaimant();
        seedItems();
        seedClaims();
        log.info("=== Heritage Link: Mock data loaded. {} items, {} claimants, {} claims ===",
                itemRepository.count(), claimantRepository.count(), claimRepository.count());
    }

    private void seedClaimant() {
        claimantRepository.save(Claimant.builder()
                .firstName("Margaret")
                .lastName("Sullivan")
                .email("margaret.sull@abc.com")
                .phone("+1-222-3333")
                .relationship("Daughter")
                .build()
        );

        claimantRepository.save(Claimant.builder()
                .firstName("Thomas")
                .lastName("Sullivan")
                .email("thomas.sull@abc.com")
                .phone("+1-222-3333")
                .relationship("Son")
                .build()
        );

        claimantRepository.save(Claimant.builder()
                .firstName("Claire")
                .lastName("Sullivan")
                .email("claire.sull@abc.com")
                .phone("+1-222-3333")
                .relationship("Granddaughter")
                .build()
        );

        claimantRepository.save(Claimant.builder()
                .firstName("Patrick")
                .lastName("Sullivan")
                .email("patrick.sull@abc.com")
                .phone("+1-222-3333")
                .relationship("Son")
                .build()
        );
    }

    private void seedItems() {

        itemRepository.save(Item.builder()
                .name("Victorian Writing Desk")
                .description("Solid walnut writing desk, circa 1890.")
                .roomOfOrigin("Study")
                .widthCm(120.0).heightCm(75.0).depthCm(60.0)
                .estimatedValue(new BigDecimal("2500.00"))
                .sentimentalScore(9)
                .status(ItemStatus.APPRAISED)
                .fragile(true)
                .specialHandlingNotes("Do not stack. Keep upright. Drawers must be secured before moving.")
                .mediationRequired(true) // pre-seeded conflict scenario
                .build());

        itemRepository.save(Item.builder()
                .name("Leather-Bound Family Bible (1923)")
                .description("Family Bible with handwritten birth records spanning five generations.")
                .roomOfOrigin("Study")
                .widthCm(35.0).heightCm(50.0).depthCm(8.0)
                .estimatedValue(new BigDecimal("150.00"))
                .sentimentalScore(10)
                .status(ItemStatus.ASSIGNED)
                .dispositionType(DispositionType.GIFTING)
                .fragile(true)
                .specialHandlingNotes("Extremely fragile binding. Wrap individually in acid-free tissue.")
                .mediationRequired(false)
                .build());

        itemRepository.save(Item.builder()
                .name("Mahogany Grandfather Clock")
                .description("Eight-day wind-up grandfather clock, Westminster chime.")
                .roomOfOrigin("Living Room")
                .widthCm(55.0).heightCm(195.0).depthCm(30.0)
                .estimatedValue(new BigDecimal("4200.00"))
                .sentimentalScore(8)
                .status(ItemStatus.ASSIGNED)
                .dispositionType(DispositionType.RELOCATION)
                .fragile(true)
                .specialHandlingNotes("Pendulum and weights must removed and packed separately.")
                .mediationRequired(false)
                .build());

        itemRepository.save(Item.builder()
                .name("Persian Wool Area Rug (9x12)")
                .description("Hand-knotted Persian rug")
                .roomOfOrigin("Living Room")
                .widthCm(365.0).heightCm(0.5).depthCm(275.0)
                .estimatedValue(new BigDecimal("3800.00"))
                .sentimentalScore(6)
                .status(ItemStatus.APPRAISED)
                .fragile(false)
                .mediationRequired(false)
                .build());

        itemRepository.save(Item.builder()
                .name("Tiffany-Style Table Lamp")
                .description("Stained-glass shade with dragonfly motif")
                .roomOfOrigin("Living Room")
                .widthCm(45.0).heightCm(65.0).depthCm(45.0)
                .estimatedValue(new BigDecimal("12000.00"))
                .sentimentalScore(7)
                .status(ItemStatus.ASSIGNED)
                .dispositionType(DispositionType.SALE)
                .fragile(true)
                .specialHandlingNotes("High-value fragile item. Requires professional art-handler packing.")
                .mediationRequired(false)
                .build());

        itemRepository.save(Item.builder()
                .name("Edwardian Dining Set")
                .description("Extending mahogany dining table with eight matching Chippendale-style chairs.")
                .roomOfOrigin("Dining Room")
                .widthCm(240.0).heightCm(78.0).depthCm(110.0)
                .estimatedValue(new BigDecimal("5500.00"))
                .sentimentalScore(5)
                .status(ItemStatus.ASSIGNED)
                .dispositionType(DispositionType.DONATION)
                .fragile(false)
                .specialHandlingNotes("Table leaves stored underneath seat frame.")
                .mediationRequired(false)
                .build());

        itemRepository.save(Item.builder()
                .name("Silver Candelabra Set")
                .description("Sterling silver five-arm candelabra")
                .roomOfOrigin("Dining Room")
                .widthCm(25.0).heightCm(45.0).depthCm(25.0)
                .estimatedValue(new BigDecimal("800.00"))
                .sentimentalScore(5)
                .status(ItemStatus.IDENTIFIED)
                .fragile(true)
                .specialHandlingNotes("Tarnish-resistant wrapping required.")
                .mediationRequired(false)
                .build());

        itemRepository.save(Item.builder()
                .name("Jewellery Armoire")
                .description("Cherry wood jewellery armoire the mirror.")
                .roomOfOrigin("Master Bedroom")
                .widthCm(50.0).heightCm(140.0).depthCm(35.0)
                .estimatedValue(new BigDecimal("650.00"))
                .sentimentalScore(6)
                .status(ItemStatus.IDENTIFIED)
                .fragile(false)
                .mediationRequired(false)
                .build());

    }

    private void seedClaims() {
        Item desk = itemRepository.findAll().stream()
                .filter(i -> i.getName().contains("Victorian Writing Desk")).findFirst().orElseThrow();
        Item bible = itemRepository.findAll().stream()
                .filter(i -> i.getName().contains("Family Bible")).findFirst().orElseThrow();

        Claimant margaret = claimantRepository.findAll().stream()
                .filter(c -> c.getFirstName().equals("Margaret")).findFirst().orElseThrow();
        Claimant thomas = claimantRepository.findAll().stream()
                .filter(c -> c.getFirstName().equals("Thomas")).findFirst().orElseThrow();
        Claimant claire = claimantRepository.findAll().stream()
                .filter(c -> c.getFirstName().equals("Claire")).findFirst().orElseThrow();

        claimRepository.save(Claim.builder()
                .item(desk).claimant(margaret).status(ClaimStatus.ACTIVE)
                .reason("This desk stood in our home study for my entire childhood")
                .build());

        claimRepository.save(Claim.builder()
                .item(desk).claimant(thomas).status(ClaimStatus.ACTIVE)
                .reason("Dad used this desk every evening for 40 years to manage the household accounts")
                .build());

        claimRepository.save(Claim.builder()
                .item(bible).claimant(claire).status(ClaimStatus.APPROVED)
                .reason("As the eldest grandchild I would like to preserve the family records.")
                .resolutionNotes("All family members agreed")
                .build());

    }
}
