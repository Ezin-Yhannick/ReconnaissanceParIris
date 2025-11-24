// ============================================
// Configuration globale de l'API
// ============================================
const API_BASE_URL = 'http://localhost:8080/api';

const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
};

// ============================================
// Gestion de l'authentification
// ============================================

/**
 * Sauvegarder les informations de l'utilisateur connecté
 */
function saveUserSession(userData) {
    localStorage.setItem('userEmail', userData.email);
    localStorage.setItem('userName', userData.nom);
    localStorage.setItem('userRole', userData.role || 'user');
    if (userData.token) {
        localStorage.setItem('authToken', userData.token);
    }
}

/**
 * Récupérer le token d'authentification
 */
function getAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * Obtenir les headers avec authentification
 */
function getAuthHeaders() {
    const token = getAuthToken();
    const headers = { ...defaultHeaders };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
}

/**
 * Vérifier si l'utilisateur est authentifié
 */
function isAuthenticated() {
    return !!localStorage.getItem('userEmail');
}

/**
 * Vérifier si l'utilisateur est admin
 */
function isAdmin() {
    return localStorage.getItem('userRole') === 'admin';
}

/**
 * Rediriger si non authentifié
 */
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

/**
 * Rediriger si non admin
 */
function requireAdmin() {
    if (!requireAuth()) return false;
    
    if (!isAdmin()) {
        window.location.href = 'dashboard-user.html';
        return false;
    }
    return true;
}

/**
 * Déconnexion
 */
function logout() {
    if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
}

// ============================================
// Fonctions API génériques
// ============================================

/**
 * Requête GET générique
 */
async function apiGet(endpoint) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Erreur API GET:', error);
        throw error;
    }
}

/**
 * Requête POST générique avec JSON
 */
async function apiPost(endpoint, data) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Erreur API POST:', error);
        throw error;
    }
}

/**
 * Requête POST avec FormData (pour upload de fichiers)
 */
async function apiPostFormData(endpoint, formData) {
    try {
        const token = getAuthToken();
        const headers = {};
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: headers,
            body: formData
        });
        
        // Toujours parser la réponse JSON
        const data = await response.json();
        
        // Si erreur HTTP, personnaliser le message
        if (!response.ok) {
            let errorMessage = data.message || `Erreur HTTP: ${response.status}`;
            
            // Personnaliser les messages selon le statut et le contenu
            if (response.status === 401) {
                if (errorMessage.includes('Aucune correspondance')) {
                    errorMessage = 'Échec de l\'authentification : Les images ne correspondent pas';
                } else if (errorMessage.includes('reconnu')) {
                    errorMessage = 'Iris non reconnu : Aucune correspondance trouvée';
                }
            } else if (response.status === 409 || errorMessage.includes('Duplicate') || errorMessage.includes('code_unique') || errorMessage.includes('déjà été enregistrée')) {
                errorMessage = 'Cette image a déjà été enregistrée pour un autre utilisateur. Veuillez utiliser une image différente.';
            } else if (response.status === 400 && errorMessage.includes('email')) {
                errorMessage = '❌ ' + errorMessage;
            }
            
            const error = new Error(errorMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }
        
        return data;
    } catch (error) {
        console.error('Erreur API POST FormData:', error);
        throw error;
    }
}

/**
 * Requête PUT générique
 */
async function apiPut(endpoint, data) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Erreur API PUT:', error);
        throw error;
    }
}

/**
 * Requête DELETE générique
 */
async function apiDelete(endpoint) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Erreur API DELETE:', error);
        throw error;
    }
}

// ============================================
// Fonctions métier - Authentification
// ============================================

/**
 * Connexion classique (email/password)
 */
async function login(email, password) {
    try {
        const data = await apiPost('/auth/login', { email, password });
        
        if (data.success) {
            saveUserSession(data.user);
            return data;
        }
        
        throw new Error(data.message || 'Erreur de connexion');
    } catch (error) {
        console.error('Erreur login:', error);
        throw error;
    }
}

/**
 * Authentification par reconnaissance d'iris
 */
async function authenticateByIris(irisImageFile) {
    try {
        const formData = new FormData();
        formData.append('irisImage', irisImageFile);
        
        const data = await apiPostFormData('/iris/authenticate', formData);
        
        if (data.success) {
            saveUserSession(data.user);
            return data;
        }
        
        throw new Error(data.message || 'Iris non reconnu');
    } catch (error) {
        console.error('Erreur authentification iris:', error);
        throw error;
    }
}

// ============================================
// Fonctions métier - Gestion des utilisateurs
// ============================================

/**
 * Enregistrer un nouvel utilisateur avec son iris
 */
async function enrollUser(userData, irisImageFile) {
    try {
        const formData = new FormData();
        formData.append('nom', userData.nom);
        formData.append('prenom', userData.prenom);
        formData.append('email', userData.email);
        formData.append('role', userData.role || 'user');
        
        if (userData.password) {
            formData.append('motDePasse', userData.password);
        }
        
        formData.append('irisImage', irisImageFile);
        
        const data = await apiPostFormData('/iris/enroll', formData);
        return data;
    } catch (error) {
        console.error('Erreur enregistrement utilisateur:', error);
        
        // Propager l'erreur avec le message personnalisé déjà formaté
        throw error;
    }
}

/**
 * Récupérer tous les utilisateurs
 */
async function getAllUsers() {
    try {
        return await apiGet('/users');
    } catch (error) {
        console.error('Erreur récupération utilisateurs:', error);
        throw error;
    }
}

/**
 * Récupérer un utilisateur par ID
 */
async function getUserById(userId) {
    try {
        return await apiGet(`/users/${userId}`);
    } catch (error) {
        console.error('Erreur récupération utilisateur:', error);
        throw error;
    }
}

/**
 * Mettre à jour un utilisateur
 */
async function updateUser(userId, userData) {
    try {
        return await apiPut(`/users/${userId}`, userData);
    } catch (error) {
        console.error('Erreur mise à jour utilisateur:', error);
        throw error;
    }
}

/**
 * Supprimer un utilisateur
 */
async function deleteUser(userId) {
    try {
        return await apiDelete(`/users/${userId}`);
    } catch (error) {
        console.error('Erreur suppression utilisateur:', error);
        throw error;
    }
}

// ============================================
// Fonctions métier - Données Iris
// ============================================

/**
 * Récupérer toutes les données iris
 */
async function getAllIrisData() {
    try {
        return await apiGet('/iris/records');
    } catch (error) {
        console.error('Erreur récupération données iris:', error);
        throw error;
    }
}

/**
 * Récupérer les données iris d'un utilisateur
 */
async function getIrisDataByUser(userId) {
    try {
        return await apiGet(`/iris/user/${userId}`);
    } catch (error) {
        console.error('Erreur récupération iris utilisateur:', error);
        throw error;
    }
}

/**
 * Comparer deux images d'iris
 */
async function compareIris(irisImage1, irisImage2) {
    try {
        const formData = new FormData();
        formData.append('irisImage1', irisImage1);
        formData.append('irisImage2', irisImage2);
        
        return await apiPostFormData('/iris/compare', formData);
    } catch (error) {
        console.error('Erreur comparaison iris:', error);
        throw error;
    }
}

// ============================================
// Fonctions métier - Logs d'authentification
// ============================================

/**
 * Récupérer tous les logs d'authentification
 */
async function getAuthLogs() {
    try {
        return await apiGet('/auth/logs');
    } catch (error) {
        console.error('Erreur récupération logs:', error);
        throw error;
    }
}

/**
 * Récupérer les logs d'un utilisateur spécifique
 */
async function getUserAuthLogs(userId) {
    try {
        return await apiGet(`/auth/logs/user/${userId}`);
    } catch (error) {
        console.error('Erreur récupération logs utilisateur:', error);
        throw error;
    }
}

// ============================================
// Utilitaires UI
// ============================================

/**
 * Afficher une notification toast
 */
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg z-50 transition-all transform translate-x-0 ${
        type === 'success' ? 'bg-green-500' : 
        type === 'error' ? 'bg-red-500' : 
        type === 'warning' ? 'bg-yellow-500' : 
        'bg-blue-500'
    } text-white font-medium`;
    
    notification.innerHTML = `
        <div class="flex items-center space-x-3">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                ${type === 'success' ? 
                    '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>' :
                    '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>'
                }
            </svg>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Animation d'entrée
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 10);
    
    // Suppression après 3 secondes
    setTimeout(() => {
        notification.style.transform = 'translateX(400px)';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

/**
 * Afficher un loader/spinner
 */
function showLoader(message = 'Chargement...') {
    const loader = document.createElement('div');
    loader.id = 'global-loader';
    loader.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    loader.innerHTML = `
        <div class="bg-white rounded-lg p-6 flex flex-col items-center space-y-4">
            <svg class="animate-spin h-10 w-10 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <p class="text-gray-700 font-medium">${message}</p>
        </div>
    `;
    
    document.body.appendChild(loader);
}

/**
 * Cacher le loader
 */
function hideLoader() {
    const loader = document.getElementById('global-loader');
    if (loader) {
        loader.remove();
    }
}

/**
 * Charger un composant HTML (header, footer)
 */
async function loadComponent(elementId, componentPath) {
    try {
        const response = await fetch(componentPath);
        if (!response.ok) {
            throw new Error(`Impossible de charger ${componentPath}`);
        }
        
        const html = await response.text();
        const element = document.getElementById(elementId);
        
        if (element) {
            element.innerHTML = html;
            
            // Exécuter les scripts du composant chargé
            const scripts = element.querySelectorAll('script');
            scripts.forEach(script => {
                const newScript = document.createElement('script');
                newScript.textContent = script.textContent;
                document.body.appendChild(newScript);
            });
        }
    } catch (error) {
        console.error(`Erreur chargement composant ${componentPath}:`, error);
    }
}

/**
 * Formater une date
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Valider un email
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Valider un fichier image
 */
function isValidImageFile(file) {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/bmp'];
    const maxSize = 5 * 1024 * 1024; // 5 MB
    
    if (!validTypes.includes(file.type)) {
        showNotification('Format d\'image non supporté. Utilisez JPG, PNG ou BMP.', 'error');
        return false;
    }
    
    if (file.size > maxSize) {
        showNotification('L\'image est trop volumineuse (max 5 MB).', 'error');
        return false;
    }
    
    return true;
}

/**
 * Prévisualiser une image
 */
function previewImage(file, imgElementId) {
    if (!isValidImageFile(file)) return;
    
    const reader = new FileReader();
    reader.onload = function(e) {
        const imgElement = document.getElementById(imgElementId);
        if (imgElement) {
            imgElement.src = e.target.result;
            imgElement.classList.remove('hidden');
        }
    };
    reader.readAsDataURL(file);
}

// ============================================
// Initialisation globale
// ============================================

// Vérifier la connexion au backend au chargement
window.addEventListener('DOMContentLoaded', async () => {
    try {
        // Test de connexion
        // await fetch(`${API_BASE_URL}/health`);
        console.log('✅ Frontend IrisAuth chargé avec succès');
    } catch (error) {
        console.warn('⚠️ Backend non disponible:', error);
    }
});

// Exporter pour utilisation globale
window.IrisAuth = {
    // Auth
    login,
    logout,
    authenticateByIris,
    isAuthenticated,
    isAdmin,
    requireAuth,
    requireAdmin,
    
    // Users
    enrollUser,
    getAllUsers,
    getUserById,
    updateUser,
    deleteUser,
    
    // Iris
    getAllIrisData,
    getIrisDataByUser,
    compareIris,
    
    // Logs
    getAuthLogs,
    getUserAuthLogs,
    
    // API génériques (AJOUTE CES LIGNES)
    apiGet,
    apiPost,
    apiPut,
    apiDelete,
    apiPostFormData,
    
    // UI
    showNotification,
    showLoader,
    hideLoader,
    loadComponent,
    formatDate,
    isValidEmail,
    isValidImageFile,
    previewImage
};

// ============================================
// Fonction pour la modale d'ajout utilisateur
// ============================================
function addUserModal() {
    return {
        showModal: false,
        currentStep: 1,
        userData: { nom: '', prenom: '', email: '', enrollmentDate: '' },
        scanMethod: 'upload',
        uploadedFile: null,
        uploadedFileName: '',
        cameraActive: false,
        videoStream: null,

        openModal() {
            this.showModal = true;
            this.currentStep = 1;
            this.resetData();
        },

        closeModal() {
            this.stopCamera();
            this.showModal = false;
            setTimeout(() => this.resetData(), 300);
        },

        resetData() {
            this.userData = { nom: '', prenom: '', email: '', enrollmentDate: '' };
            this.uploadedFile = null;
            this.uploadedFileName = '';
            this.scanMethod = 'upload';
            this.currentStep = 1;
        },

        async validateUserData() {
            if (!this.userData.nom || !this.userData.prenom || !this.userData.email) {
                showNotification('Veuillez remplir tous les champs obligatoires', 'error');
                return;
            }
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(this.userData.email)) {
                showNotification('Veuillez entrer une adresse email valide', 'error');
                return;
            }
            try {
                const response = await apiGet(`/users/check-email?email=${encodeURIComponent(this.userData.email)}`);
                if (response.exists) {
                    showNotification('Cet email est déjà utilisé. Veuillez en choisir un autre.', 'error');
                    return;
                }
                this.currentStep = 2;
            } catch (error) {
                console.error('Erreur vérification email:', error);
                showNotification('Erreur lors de la vérification. Réessayez.', 'error');
            }
        },

        confirmAndContinue() {
            showNotification('Informations validées !', 'success');
            this.currentStep = 3;
        },

        handleFileUpload(event) {
            const file = event.target.files[0];
            if (file) {
                this.uploadedFile = file;
                this.uploadedFileName = file.name;
            }
        },

        async toggleCamera() {
            if (this.cameraActive) {
                this.stopCamera();
            } else {
                await this.startCamera();
            }
        },

        async startCamera() {
            try {
                this.videoStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' } });
                this.$refs.videoElement.srcObject = this.videoStream;
                this.cameraActive = true;
            } catch (error) {
                showNotification('Impossible d\'accéder à la caméra', 'error');
            }
        },

        stopCamera() {
            if (this.videoStream) {
                this.videoStream.getTracks().forEach(track => track.stop());
                this.videoStream = null;
            }
            this.cameraActive = false;
        },

        async completeEnrollment() {
            if (!this.uploadedFile) return;
            try {
                const userData = { nom: this.userData.nom, prenom: this.userData.prenom, email: this.userData.email, role: 'user' };
                const result = await enrollUser(userData, this.uploadedFile);
                if (result.status === 'success') {
                    const now = new Date();
                    this.userData.enrollmentDate = now.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' }) + ' à ' + now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
                    this.currentStep = 4;
                    window.dispatchEvent(new CustomEvent('user-added', { detail: this.userData }));
                    showNotification('Utilisateur enrôlé avec succès !', 'success');
                } else {
                    showNotification(result.message || 'Erreur lors de l\'enrôlement', 'error');
                }
            } catch (error) {
                console.error('Erreur enrôlement:', error);
                showNotification(error.message || 'Erreur lors de l\'enrôlement', 'error');
            }
        },

        async captureAndEnroll() {
            if (!this.cameraActive) return;
            try {
                const canvas = document.createElement('canvas');
                const video = this.$refs.videoElement;
                canvas.width = video.videoWidth;
                canvas.height = video.videoHeight;
                canvas.getContext('2d').drawImage(video, 0, 0);
                const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg'));
                const file = new File([blob], 'iris-capture.jpg', { type: 'image/jpeg' });
                this.stopCamera();
                const userData = { nom: this.userData.nom, prenom: this.userData.prenom, email: this.userData.email, role: 'user' };
                const result = await enrollUser(userData, file);
                if (result.status === 'success') {
                    const now = new Date();
                    this.userData.enrollmentDate = now.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' }) + ' à ' + now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
                    this.currentStep = 4;
                    window.dispatchEvent(new CustomEvent('user-added', { detail: this.userData }));
                    showNotification('Utilisateur enrôlé avec succès !', 'success');
                } else {
                    showNotification(result.message || 'Erreur lors de l\'enrôlement', 'error');
                }
            } catch (error) {
                console.error('Erreur capture:', error);
                showNotification(error.message || 'Erreur lors de la capture', 'error');
            }
        }
    }
}

// Rendre la fonction disponible globalement
window.addUserModal = addUserModal;

// ============================================
// Fonction pour le header
// ============================================
function headerComponent() {
    return {
        userName: 'Administrateur',
        
        init() {
            const email = localStorage.getItem('userEmail') || 'admin@iris-auth.com';
            const name = email.split('@')[0];
            this.userName = name.charAt(0).toUpperCase() + name.slice(1);
        },
        
        openAddUserModal() {
            window.dispatchEvent(new CustomEvent('open-add-user-modal'));
        },
                        
        logout() {
            if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
                localStorage.clear();
                window.location.href = 'index.html';
            }
        }
    }
}

// Rendre la fonction disponible globalement
window.headerComponent = headerComponent;