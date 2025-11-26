document.addEventListener("DOMContentLoaded", () => {
    const registrationForm = document.getElementById("registrationForm");
    const registrationStatus = document.getElementById("registrationStatus");
    const successInfo = document.getElementById("successInfo");
    const appHashDisplay = document.getElementById("appHashDisplay");
    const integrationUrl = document.getElementById("integrationUrl");
    const copyHashBtn = document.getElementById("copyHashBtn");
    const copyUrlBtn = document.getElementById("copyUrlBtn");
    const submitBtn = registrationForm.querySelector('button[type="submit"]');
    const templateFileInput = document.getElementById("templateFile");
    
    // Check form completion and toggle button state
    function checkFormCompletion() {
        const appName = document.getElementById("appName").value.trim();
        const category = document.getElementById("category").value.trim();
        const endpoint = document.getElementById("endpoint").value.trim();
        const templateFile = templateFileInput.files[0];
        
        if (appName && category && endpoint && templateFile) {
            submitBtn.classList.remove("btn-faded");
            submitBtn.classList.add("btn-active");
        } else {
            submitBtn.classList.remove("btn-active");
            submitBtn.classList.add("btn-faded");
        }
    }
    
    // Add event listeners to all form inputs
    document.getElementById("appName").addEventListener("input", checkFormCompletion);
    document.getElementById("category").addEventListener("input", checkFormCompletion);
    document.getElementById("endpoint").addEventListener("input", checkFormCompletion);
    templateFileInput.addEventListener("change", handleFileChange);
    
    async function handleFileChange() {
        checkFormCompletion();
        const file = templateFileInput.files[0];
        if (file && (file.name.endsWith('.csv') || file.name.endsWith('.xlsx'))) {
            await extractAndDisplayHeaders(file);
        } else {
            document.getElementById('headerConfigSection').style.display = 'none';
        }
    }
    
    async function extractAndDisplayHeaders(file) {
        try {
            const formData = new FormData();
            formData.append('file', file);
            
            const response = await fetch('http://localhost:8081/api/register/extract-headers', {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                const headers = await response.json();
                console.log('Extracted headers:', headers);
                displayHeaderGrid(headers);
            } else {
                console.error('Failed to extract headers, status:', response.status);
            }
        } catch (error) {
            console.error('Failed to extract headers:', error);
        }
    }
    
    function displayHeaderGrid(headers) {
        console.log('Displaying headers in grid:', headers);
        const headerRows = document.getElementById('headerRows');
        headerRows.innerHTML = '';
        
        headers.forEach((header, index) => {
            console.log(`Header ${index}:`, header);
            const isGenericHeader = /^Column\d+$/i.test(header.trim());
            const row = document.createElement('div');
            row.className = 'header-row';
            
            if (isGenericHeader) {
                row.innerHTML = `
                    <div class="header-name">
                        <input type="text" name="headerName_${index}" placeholder="Enter header name" value="${header}" style="width: 100%; padding: 0.25rem; border: 1px solid #ccc; border-radius: 4px;">
                    </div>
                    <div>
                        <select name="fieldType_${index}">
                            <option value="string">String</option>
                            <option value="integer">Integer</option>
                            <option value="decimal">Decimal</option>
                            <option value="date">Date</option>
                            <option value="email">Email</option>
                        </select>
                    </div>
                    <div><input type="checkbox" name="nullAllowed_${index}" checked></div>
                    <div><input type="checkbox" name="required_${index}"></div>
                    <div><input type="checkbox" name="specialChars_${index}" checked></div>
                `;
            } else {
                row.innerHTML = `
                    <div class="header-name">${header}</div>
                    <div>
                        <select name="fieldType_${header}">
                            <option value="string">String</option>
                            <option value="integer">Integer</option>
                            <option value="decimal">Decimal</option>
                            <option value="date">Date</option>
                            <option value="email">Email</option>
                        </select>
                    </div>
                    <div><input type="checkbox" name="nullAllowed_${header}" checked></div>
                    <div><input type="checkbox" name="required_${header}"></div>
                    <div><input type="checkbox" name="specialChars_${header}" checked></div>
                `;
            }
            headerRows.appendChild(row);
        });
        
        document.getElementById('headerConfigSection').style.display = 'block';
    }
    
    function collectHeaderConfiguration() {
        const headerConfigSection = document.getElementById('headerConfigSection');
        if (headerConfigSection.style.display === 'none') {
            return null;
        }
        
        const config = {};
        const headerRows = document.querySelectorAll('.header-row');
        
        headerRows.forEach((row, index) => {
            const headerNameInput = row.querySelector(`input[name="headerName_${index}"]`);
            const headerName = headerNameInput ? headerNameInput.value.trim() : row.querySelector('.header-name').textContent;
            
            if (!headerName) return;
            
            const fieldTypeSelect = row.querySelector(`select[name="fieldType_${headerName}"]`) || row.querySelector(`select[name="fieldType_${index}"]`);
            const nullAllowedInput = row.querySelector(`input[name="nullAllowed_${headerName}"]`) || row.querySelector(`input[name="nullAllowed_${index}"]`);
            const requiredInput = row.querySelector(`input[name="required_${headerName}"]`) || row.querySelector(`input[name="required_${index}"]`);
            const specialCharsInput = row.querySelector(`input[name="specialChars_${headerName}"]`) || row.querySelector(`input[name="specialChars_${index}"]`);
            
            config[headerName] = {
                fieldType: fieldTypeSelect ? fieldTypeSelect.value : 'string',
                nullAllowed: nullAllowedInput ? nullAllowedInput.checked : true,
                required: requiredInput ? requiredInput.checked : false,
                specialChars: specialCharsInput ? specialCharsInput.checked : true
            };
        });
        
        return config;
    }

    async function validateTemplateAgainstConfig(templateFile, headerConfig) {
        if (!headerConfig) return { valid: true };
        
        try {
            const formData = new FormData();
            formData.append('file', templateFile);
            formData.append('fieldConfig', JSON.stringify(headerConfig));
            
            const response = await fetch('http://localhost:8081/api/register/validate-template', {
                method: 'POST',
                body: formData
            });
            
            return await response.json();
        } catch (error) {
            console.error('Template validation error:', error);
            return { valid: false, message: 'Failed to validate template' };
        }
    }

    registrationForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        
        const appName = document.getElementById("appName").value.trim();
        const category = document.getElementById("category").value.trim();
        const endpoint = document.getElementById("endpoint").value.trim();
        const templateFile = document.getElementById("templateFile").files[0];
        
        if (!appName || !category || !endpoint || !templateFile) {
            showStatus("Please fill all fields and select a template file.", "error");
            return;
        }
        
        // Collect header configuration if available
        const headerConfig = collectHeaderConfiguration();
        
        // Validate template against field configuration
        if (headerConfig) {
            const submitBtn = registrationForm.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.textContent = "Validating Template...";
            
            const validation = await validateTemplateAgainstConfig(templateFile, headerConfig);
            
            if (!validation.valid) {
                showStatus(`Template validation failed: ${validation.message}`, "error");
                submitBtn.disabled = false;
                submitBtn.textContent = "Register Template";
                return;
            }
            
            submitBtn.textContent = "Registering...";
        }
        
        const formData = new FormData();
        formData.append("appName", appName);
        formData.append("category", category);
        formData.append("endpoint", endpoint);
        formData.append("template", templateFile);
        
        if (headerConfig) {
            formData.append("headerConfig", JSON.stringify(headerConfig));
        }
        
        const submitBtn = registrationForm.querySelector('button[type="submit"]');
        if (!submitBtn.disabled) {
            submitBtn.disabled = true;
            submitBtn.textContent = "Registering...";
        }
        
        try {
            const response = await fetch("http://localhost:8081/api/register", {
                method: "POST",
                body: formData
            });
            
            const result = await response.json();
            
            if (result.success) {
                showStatus("Registration successful!", "success");
                showSuccessInfo(result.appNameHash, appName);
                registrationForm.reset();
                document.getElementById('headerConfigSection').style.display = 'none';
                checkFormCompletion(); // Reset button state
            } else {
                showStatus(`Registration failed: ${result.message}`, "error");
            }
            
        } catch (error) {
            console.error("Registration error:", error);
            showStatus("Registration failed. Please check if services are running.", "error");
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Register Template";
        }
    });
    
    function showStatus(message, type) {
        registrationStatus.textContent = message;
        registrationStatus.className = `status-message ${type}`;
        
        // Hide success info if showing error
        if (type === "error") {
            successInfo.style.display = "none";
        }
    }
    
    function showSuccessInfo(appHash, appName) {
        appHashDisplay.value = appHash;
        integrationUrl.value = `${window.location.origin}/index.html?app=${appHash}`;
        successInfo.style.display = "block";
    }
    
    // Copy functionality
    copyHashBtn.addEventListener("click", () => {
        appHashDisplay.select();
        document.execCommand("copy");
        copyHashBtn.textContent = "Copied!";
        setTimeout(() => {
            copyHashBtn.textContent = "Copy";
        }, 2000);
    });
    
    copyUrlBtn.addEventListener("click", () => {
        integrationUrl.select();
        document.execCommand("copy");
        copyUrlBtn.textContent = "Copied!";
        setTimeout(() => {
            copyUrlBtn.textContent = "Copy";
        }, 2000);
    });
});