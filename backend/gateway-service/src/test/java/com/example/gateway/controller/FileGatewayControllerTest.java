// package com.example.gateway.controller;

// import com.example.gateway.dto.ExtractedFile;
// import com.example.gateway.dto.TemplateDefinition;
// import com.example.gateway.service.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(FileGatewayController.class)
// class FileGatewayControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private FileValidationService validationService;

//     @MockBean
//     private TemplateLookupService templateLookupService;

//     @MockBean
//     private ExtractionService extractionService;

//     @MockBean
//     private StructureValidationService structureValidationService;

//     @MockBean
//     private EndpointNotificationService endpointNotificationService;

//     private TemplateDefinition mockTemplate;
//     private ExtractedFile mockExtractedFile;

//     @BeforeEach
//     void setUp() {
//         mockTemplate = new TemplateDefinition();
//         mockTemplate.setTemplateName("Employee Template");
//         mockTemplate.setFileType("csv");
//         mockTemplate.setHeaders(Arrays.asList("Name", "Email", "Department"));
        
//         Map<String, Object> rules = new HashMap<>();
//         rules.put("minRows", 1);
//         rules.put("maxRows", 1000);
//         rules.put("strictColumnOrder", true);
//         rules.put("allowExtraColumns", false);
//         mockTemplate.setStructureRules(rules);

//         mockExtractedFile = new ExtractedFile();
//         mockExtractedFile.setHeaders(Arrays.asList("Name", "Email", "Department"));
        
//         List<Map<String, Object>> rows = Arrays.asList(
//             createRow("John Doe", "john@example.com", "IT"),
//             createRow("Jane Smith", "jane@example.com", "HR")
//         );
//         mockExtractedFile.setRows(rows);
//     }

//     @Test
//     void testSuccessfulFileUpload() throws Exception {
//         MockMultipartFile file = new MockMultipartFile(
//             "file", "test.csv", "text/csv", "Name,Email,Department\nJohn,john@test.com,IT".getBytes()
//         );

//         when(templateLookupService.fetchAppTemplate("testApp", "employee")).thenReturn(mockTemplate);
//         when(extractionService.extractWithHeaders(any(), eq("csv"), any())).thenReturn(mockExtractedFile);
//         when(endpointNotificationService.sendDataToAppEndpoint(any(), any(), any())).thenReturn(true);

//         mockMvc.perform(multipart("/api/gateway/upload")
//                 .file(file)
//                 .param("application", "testApp")
//                 .param("category", "employee")
//                 .param("appNameHash", "testApp"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.success").value(true))
//                 .andExpect(jsonPath("$.message").value("File validated and data sent to application successfully"));
//     }

//     @Test
//     void testFileUploadWithInvalidFile() throws Exception {
//         MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "".getBytes());
        
//         doThrow(new IllegalArgumentException("File is empty"))
//             .when(validationService).validate(any(), any(), any());

//         mockMvc.perform(multipart("/api/gateway/upload")
//                 .file(file)
//                 .param("application", "testApp")
//                 .param("category", "employee")
//                 .param("appNameHash", "testApp"))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.success").value(false))
//                 .andExpect(jsonPath("$.message").value("File is empty"));
//     }

//     @Test
//     void testFetchAppCategories() throws Exception {
//         List<String> categories = Arrays.asList("employee", "invoice", "payment");
//         when(templateLookupService.fetchAppCategories("testApp")).thenReturn(categories);

//         mockMvc.perform(get("/api/gateway/templates/categories/testApp"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$.length()").value(3));
//     }

//     private Map<String, Object> createRow(String name, String email, String department) {
//         Map<String, Object> row = new HashMap<>();
//         row.put("Name", name);
//         row.put("Email", email);
//         row.put("Department", department);
//         return row;
//     }
// }