package com.project.heritagelink.service;

import com.project.heritagelink.model.Claimant;
import com.project.heritagelink.model.Item;
import com.project.heritagelink.repository.ClaimRepository;
import com.project.heritagelink.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimService")
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ClaimantService claimantService;
    @Mock private ItemService itemService;
    @Mock private AuditService auditService;

    @InjectMocks private ClaimService claimService;

    private Item highSentimentItem;
    private Item lowSentimentItem;
    private Claimant margaret;
    private Claimant thomas;

    @BeforeEach
    void setUp() {

    }

}
