document.addEventListener("DOMContentLoaded", () => {
    // Get app name hash from URL
    const urlParams = new URLSearchParams(window.location.search);
    const appNameHash = urlParams.get('app');

    if (!appNameHash) {
        return; // Error already handled in HTML
    }

    // DOM elements
    const categorySelect = document.getElementById("categorySelect");
    const selectionError = document.getElementById("selectionError");

    const templateSection = document.getElementById("templateSection");
    const templateNameEl = document.getElementById("templateName");
    const headerPreview = document.getElementById("headerPreview");
    const downloadBtn = document.getElementById("downloadBtn");
    const downloadStatus = document.getElementById("downloadStatus");

    const fileInput = document.getElementById("fileInput");
    const uploadBtn = document.getElementById("uploadBtn");
    const uploadStatus = document.getElementById("uploadStatus");

    let currentTemplate = null;

    // =============================
    // 1. Load categories for dropdown
    // =============================
    function loadCategories() {
        const loadingOption = document.createElement("option");
        loadingOption.textContent = "Loading categories...";
        loadingOption.disabled = true;
        loadingOption.selected = true;
        categorySelect.appendChild(loadingOption);

        fetch(`http://localhost:8080/api/templates/app/${appNameHash}/categories`)
            .then((response) => {
                if (!response.ok) throw new Error("Network response was not ok");
                return response.json();
            })
            .then((categories) => {
                categorySelect.innerHTML = '<option value="">Select Category</option>';

                if (categories.length === 0) {
                    categorySelect.innerHTML = '<option value="" disabled>No templates available for this app</option>';
                    return;
                }

                categories.forEach((category) => {
                    const option = document.createElement("option");
                    option.value = category;
                    option.textContent = category;
                    categorySelect.appendChild(option);
                });
            })
            .catch((err) => {
                console.error("Error fetching categories:", err);
                selectionError.textContent = "Could not load categories for this app. Please check if the app is registered.";
                categorySelect.innerHTML = '<option value="" disabled>No categories available</option>';
            });
    }

    // =============================
    // 2. Handle template selection
    // =============================
    function handleTemplateSelection() {
        const category = categorySelect.value;

        if (!category) {
            templateSection.style.display = "none";
            currentTemplate = null;
            return;
        }

        selectionError.textContent = "";
        templateSection.style.display = "block";

        // Load template metadata (filetype will be determined from template)
        loadTemplateMetadata(category, null);
    }

    function loadTemplateMetadata(category, filetype) {
        templateNameEl.textContent = "Loading...";
        headerPreview.innerHTML = '<span class="placeholder-text">Loading template...</span>';
        // headerPreview.innerHTML = `<div class="table-container">${tableHTML}</div>`;



        // Fetch full template data using app-specific endpoint
        const url = `http://localhost:8080/api/templates/app/${appNameHash}/${encodeURIComponent(category)}/json`;

        fetch(url)
            .then((response) => {
                if (!response.ok) throw new Error("Failed to fetch template");
                return response.json();
            })
            .then((templateData) => {
                // Also fetch metadata to get file type
                return fetch(`http://localhost:8080/api/templates/app/${appNameHash}/${encodeURIComponent(category)}/metadata`)
                    .then(response => response.json())
                    .then(metadata => {
                        currentTemplate = {
                            appNameHash,
                            category,
                            filetype: metadata.fileType,
                            data: templateData
                        };

                        templateNameEl.textContent = `${category} Template (${metadata.fileType.toUpperCase()})`;

                        // Render full template as table - works for all formats including .xls
                        renderTemplateTable(templateData);
                    });
            })
            .catch((err) => {
                console.error("Error fetching template:", err);
                selectionError.textContent = "Could not load template for this app.";
                templateSection.style.display = "none";
                currentTemplate = null;
            });
    }

    function renderTemplateTable(data) {
        if (!Array.isArray(data) || data.length === 0) {
            headerPreview.innerHTML = '<span class="placeholder-text">No template data available</span>';
            return;
        }

        const headers = Object.keys(data[0]);

        let tableHTML = '<table class="template-table">';

        // Header row
        tableHTML += '<thead><tr>';
        headers.forEach(header => {
            tableHTML += `<th>${header}</th>`;
        });
        tableHTML += '</tr></thead>';

        // Data rows (show first 5 rows as preview)
        tableHTML += '<tbody>';
        const previewRows = data.slice(0, 5);
        previewRows.forEach(row => {
            tableHTML += '<tr>';
            headers.forEach(header => {
                tableHTML += `<td>${row[header] || ''}</td>`;
            });
            tableHTML += '</tr>';
        });

        if (data.length > 5) {
            tableHTML += `<tr><td colspan="${headers.length}" class="more-rows">... and ${data.length - 5} more rows</td></tr>`;
        }

        tableHTML += '</tbody></table>';

        // headerPreview.innerHTML = tableHTML;
        headerPreview.innerHTML = `<div class="table-container">${tableHTML}</div>`;
    }

    // =============================
    // 3. Template download
    // =============================
    downloadBtn.addEventListener("click", () => {
        if (!currentTemplate) return;

        downloadStatus.textContent = "";
        downloadStatus.className = "status-message";
        downloadBtn.disabled = true;
        downloadBtn.textContent = "Downloading...";

        const url = `http://localhost:8080/api/templates/app/${currentTemplate.appNameHash}/${encodeURIComponent(currentTemplate.category)}/download`;

        fetch(url)
            .then((response) => {
                if (!response.ok) throw new Error("Download failed");
                return response.blob();
            })
            .then((blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = `${currentTemplate.category}-template.${currentTemplate.filetype || 'csv'}`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);

                downloadStatus.textContent = "Template downloaded successfully!";
                downloadStatus.className = "status-message success";
            })
            .catch((err) => {
                console.error("Download error:", err);
                downloadStatus.textContent = "Download failed. Please try again.";
                downloadStatus.className = "status-message error";
            })
            .finally(() => {
                downloadBtn.disabled = false;
                downloadBtn.textContent = "Download Template";
            });
    });

    // =============================
    // 4. File upload
    // =============================
    uploadBtn.addEventListener("click", () => {
        uploadStatus.textContent = "";
        uploadStatus.className = "status-message";

        const file = fileInput.files[0];
        const category = categorySelect.value;

        if (!file) {
            uploadStatus.textContent = "Please select a file to upload.";
            uploadStatus.className = "status-message error";
            return;
        }

        if (!category) {
            uploadStatus.textContent = "Please select a category first.";
            uploadStatus.className = "status-message error";
            return;
        }

        // Validate file type - ensure .xls files are accepted
        const fileName = file.name.toLowerCase();
        const allowedExtensions = ['.csv', '.xls', '.xlsx', '.txt'];
        const isValidFile = allowedExtensions.some(ext => fileName.endsWith(ext));
        
        if (!isValidFile) {
            uploadStatus.textContent = "Please select a valid file type (.csv, .xls, .xlsx, or .txt).";
            uploadStatus.className = "status-message error";
            return;
        }

        const formData = new FormData();
        formData.append("file", file);
        formData.append("application", appNameHash); // Use app hash as application identifier
        formData.append("category", category);
        formData.append("appNameHash", appNameHash);

        uploadBtn.disabled = true;
        uploadBtn.textContent = "Uploading...";

        fetch("http://localhost:8080/api/gateway/upload", {
            method: "POST",
            body: formData
        })
            .then(async (response) => {
                let result;
                try {
                    result = await response.json();
                } catch (e) {
                    // If JSON parsing fails, try to get text response
                    const textResponse = await response.text();
                    console.error("Server response (not JSON):", textResponse);
                    result = { message: textResponse || "Server error occurred" };
                }
                
                console.log("Response status:", response.status);
                console.log("Response data:", result);

                if (!response.ok) {
                    // Extract and categorize error message
                    let errorMessage = result.message || "Upload failed";
                    let errorCategory = "Upload Error";
                    
                    // Check if it's actually a success with notification failure
                    if (errorMessage.includes('File validated but failed to notify application') || 
                        errorMessage.includes('converted to json but failed to notify application')) {
                        // This is actually a success - data was processed correctly
                        const successMessage = "File uploaded and validated successfully! (Note: Application notification failed but your data was processed correctly)";
                        uploadStatus.textContent = successMessage;
                        uploadStatus.className = "status-message success";
                        showToast(successMessage, 'success');
                        
                        // Log the JSON data to console
                        console.log("=== UPLOAD RESPONSE ===");
                        console.log("Full Response:", result);
                        if (result.data) {
                            console.log("Extracted JSON Data:", result.data);
                            console.log("JSON as String:", JSON.stringify(result.data, null, 2));
                        }
                        
                        // Clear form and disable upload button
                        fileInput.value = "";
                        uploadBtn.disabled = true;
                        return; // Don't throw error
                    }
                    
                    // Categorize different types of errors
                    if (errorMessage.includes('Field validation failed:')) {
                        errorCategory = "❌ Data Validation Failed";
                        const lines = errorMessage.split('\n');
                        const errorLines = lines.slice(1, 4); // Skip header, show first 3 errors
                        errorMessage = `${errorCategory}\n${errorLines.join('\n')}`;
                        if (lines.length > 4) {
                            errorMessage += '\n... and ' + (lines.length - 4) + ' more validation errors';
                        }
                    } else if (errorMessage.includes('File size exceeds')) {
                        errorCategory = "📁 File Size Error";
                        errorMessage = `${errorCategory}\n${errorMessage}`;
                    } else if (errorMessage.includes('File format mismatch') || errorMessage.includes('Unsupported file type')) {
                        errorCategory = "📄 File Format Error";
                        errorMessage = `${errorCategory}\n${errorMessage}`;
                    } else if (errorMessage.includes('Header validation failed') || errorMessage.includes('Column mismatch')) {
                        errorCategory = "📋 Template Structure Error";
                        errorMessage = `${errorCategory}\n${errorMessage}`;
                    } else if (errorMessage.includes('Template not found') || errorMessage.includes('Template metadata is null')) {
                        errorCategory = "🔍 Template Not Found";
                        errorMessage = `${errorCategory}\nThe selected template is not available. Please contact support.`;
                    } else if (errorMessage.includes('Application') && errorMessage.includes('not allowed')) {
                        errorCategory = "🚫 Access Denied";
                        errorMessage = `${errorCategory}\n${errorMessage}`;
                    } else {
                        errorCategory = "⚠️ Upload Failed";
                        errorMessage = `${errorCategory}\n${errorMessage}`;
                    }
                    
                    throw new Error(errorMessage);
                }

                const successMessage = result.message || "File uploaded and validated successfully!";
                uploadStatus.textContent = successMessage;
                uploadStatus.className = "status-message success";
                
                // Show success toast
                showToast(successMessage, 'success');

                // Display the actual JSON response data
                console.log("=== UPLOAD RESPONSE ===");
                console.log("Full Response:", result);
                if (result.data) {
                    console.log("Extracted JSON Data:", result.data);
                    console.log("JSON as String:", JSON.stringify(result.data, null, 2));
                }
                
                // Clear form and disable upload button
                fileInput.value = "";
                uploadBtn.disabled = true;
            })
            .catch((err) => {
                console.error("Upload error:", err);
                const errorMessage = err.message || "Upload failed. Please try again.";
                uploadStatus.textContent = errorMessage;
                uploadStatus.className = "status-message error";
                
                // Show failure toast with detailed error
                showToast(errorMessage, 'error');
            })
            .finally(() => {
                uploadBtn.textContent = "Upload File";
                // Check if file is selected to determine disabled state
                uploadBtn.disabled = fileInput.files.length === 0;
            });
    });

    // =============================
    // Event listeners
    // =============================
    categorySelect.addEventListener("change", () => {
        handleTemplateSelection();
        // Force chevron back to default after selection
        categorySelect.blur();
    });

    // Show/fade upload button based on file selection
    fileInput.addEventListener("change", () => {
        if (fileInput.files.length > 0) {
            uploadBtn.disabled = false;
        } else {
            uploadBtn.disabled = true;
        }
    });

    // Initialize
    loadCategories();
});