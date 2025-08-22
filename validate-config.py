#!/usr/bin/env python3
"""
Aalay Configuration Validator
Validates that all required environment variables and configuration files are properly set up.
"""

import os
import sys
import json
from pathlib import Path

class ConfigValidator:
    def __init__(self, project_root):
        self.project_root = Path(project_root)
        self.errors = []
        self.warnings = []
        self.info = []
        
    def validate_env_file(self):
        """Validate .env file exists and has required variables"""
        env_file = self.project_root / '.env'
        env_template = self.project_root / '.env.template'
        
        if not env_file.exists():
            if env_template.exists():
                self.warnings.append("No .env file found. Copy .env.template to .env and fill in your values.")
            else:
                self.errors.append("Neither .env nor .env.template file found!")
            return
            
        self.info.append("✓ .env file found")
        
        # Read and validate env file
        required_vars = [
            'API_BASE_URL_PROD',
            'MAPBOX_ACCESS_TOKEN_PROD',
            'FIREBASE_PROJECT_ID_PROD'
        ]
        
        env_vars = {}
        try:
            with open(env_file, 'r') as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith('#') and '=' in line:
                        key, value = line.split('=', 1)
                        env_vars[key.strip()] = value.strip()
        except Exception as e:
            self.errors.append(f"Error reading .env file: {e}")
            return
            
        # Check required variables
        for var in required_vars:
            if var not in env_vars:
                self.warnings.append(f"Required variable {var} not found in .env")
            elif env_vars[var] in ['', 'your_placeholder_value', 'placeholder']:
                self.warnings.append(f"Variable {var} has placeholder value")
        
        self.info.append(f"✓ Found {len(env_vars)} environment variables")
        
    def validate_firebase_config(self):
        """Validate Firebase configuration"""
        google_services = self.project_root / 'app' / 'google-services.json'
        google_services_template = self.project_root / 'app' / 'google-services.json.template'
        
        if not google_services.exists():
            if google_services_template.exists():
                self.warnings.append("No google-services.json found. Copy template and configure with your Firebase project.")
            else:
                self.warnings.append("No Firebase configuration found!")
        else:
            self.info.append("✓ google-services.json found")
            
            # Validate JSON structure
            try:
                with open(google_services, 'r') as f:
                    config = json.load(f)
                    
                if 'project_info' in config and 'client' in config:
                    project_id = config['project_info'].get('project_id', '')
                    if project_id and project_id != 'aalay-student-housing':
                        self.info.append(f"✓ Firebase project ID: {project_id}")
                    else:
                        self.warnings.append("Firebase project_id appears to be a placeholder")
                else:
                    self.warnings.append("google-services.json has invalid structure")
                    
            except json.JSONDecodeError:
                self.errors.append("google-services.json is not valid JSON")
            except Exception as e:
                self.warnings.append(f"Could not validate google-services.json: {e}")
                
    def validate_build_files(self):
        """Validate build configuration files"""
        required_files = [
            'build.gradle',
            'app/build.gradle',
            'settings.gradle',
            'gradle.properties',
            'env-loader.gradle'
        ]
        
        for file_path in required_files:
            full_path = self.project_root / file_path
            if full_path.exists():
                self.info.append(f"✓ {file_path} found")
            else:
                self.errors.append(f"Required build file missing: {file_path}")
                
    def validate_security_files(self):
        """Validate security configuration files"""
        security_files = [
            'app/src/main/res/xml/network_security_config.xml',
            'app/src/main/res/xml/backup_rules.xml',
            'app/src/main/res/xml/data_extraction_rules.xml',
            'app/proguard-rules.pro'
        ]
        
        for file_path in security_files:
            full_path = self.project_root / file_path
            if full_path.exists():
                self.info.append(f"✓ {file_path} found")
            else:
                self.warnings.append(f"Security file missing: {file_path}")
                
    def validate_source_files(self):
        """Validate critical source files"""
        critical_files = [
            'utils/ConfigManager.kt',
            'utils/SecurityConfig.kt',
            'di/NetworkModule.kt',
            'AalayApplication.kt'
        ]
        
        for file_path in critical_files:
            full_path = self.project_root / file_path
            if full_path.exists():
                self.info.append(f"✓ {file_path} found")
            else:
                self.errors.append(f"Critical source file missing: {file_path}")
                
    def validate_documentation(self):
        """Validate documentation files"""
        doc_files = [
            'README.md',
            'DEPLOYMENT.md',
            '.gitignore'
        ]
        
        for file_path in doc_files:
            full_path = self.project_root / file_path
            if full_path.exists():
                self.info.append(f"✓ {file_path} found")
            else:
                self.warnings.append(f"Documentation file missing: {file_path}")
                
    def validate_ci_cd(self):
        """Validate CI/CD configuration"""
        ci_files = [
            '.github/workflows/android-ci.yml'
        ]
        
        for file_path in ci_files:
            full_path = self.project_root / file_path
            if full_path.exists():
                self.info.append(f"✓ {file_path} found")
            else:
                self.warnings.append(f"CI/CD file missing: {file_path}")
                
    def validate_all(self):
        """Run all validations"""
        print("🔍 Validating Aalay project configuration...\n")
        
        self.validate_env_file()
        self.validate_firebase_config()
        self.validate_build_files()
        self.validate_security_files()
        self.validate_source_files()
        self.validate_documentation()
        self.validate_ci_cd()
        
        # Print results
        if self.info:
            print("✅ Configuration Status:")
            for msg in self.info:
                print(f"   {msg}")
            print()
            
        if self.warnings:
            print("⚠️  Warnings:")
            for msg in self.warnings:
                print(f"   {msg}")
            print()
            
        if self.errors:
            print("❌ Errors:")
            for msg in self.errors:
                print(f"   {msg}")
            print()
            
        # Summary
        total_checks = len(self.info) + len(self.warnings) + len(self.errors)
        success_rate = (len(self.info) / total_checks * 100) if total_checks > 0 else 0
        
        print(f"📊 Summary: {len(self.info)} passed, {len(self.warnings)} warnings, {len(self.errors)} errors")
        print(f"🎯 Success rate: {success_rate:.1f}%")
        
        if self.errors:
            print("\n❗ Critical errors found. Please fix them before building.")
            return False
        elif self.warnings:
            print("\n⚡ Warnings found. Consider addressing them for optimal setup.")
            return True
        else:
            print("\n🎉 All checks passed! Your configuration looks good.")
            return True

def main():
    if len(sys.argv) > 1:
        project_root = sys.argv[1]
    else:
        project_root = os.getcwd()
        
    validator = ConfigValidator(project_root)
    success = validator.validate_all()
    
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()