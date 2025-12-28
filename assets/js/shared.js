
// Backend API Base URL
const API_BASE = 'http://localhost:9090/projet-planning-intelligent';
// Current logged-in user ID (default: 2, should be set from login page)
let CURRENT_USER_ID = localStorage.getItem('userId') || '2';

// ============ TOAST NOTIFICATIONS ============
let __toastContainer = null;
function ensureToastContainer() {
  if (__toastContainer) return __toastContainer;
  __toastContainer = document.createElement('div');
  __toastContainer.style.position = 'fixed';
  __toastContainer.style.top = '16px';
  __toastContainer.style.right = '16px';
  __toastContainer.style.zIndex = '9999';
  __toastContainer.style.display = 'flex';
  __toastContainer.style.flexDirection = 'column';
  __toastContainer.style.gap = '8px';
  document.body.appendChild(__toastContainer);
  return __toastContainer;
}

function showToast(message, type = 'info') {
  try {
    const container = ensureToastContainer();
    const toast = document.createElement('div');
    toast.textContent = message;
    toast.style.padding = '10px 12px';
    toast.style.borderRadius = '6px';
    toast.style.boxShadow = '0 6px 20px rgba(0,0,0,0.15)';
    toast.style.color = '#0b1220';
    toast.style.fontSize = '13px';
    toast.style.borderLeft = '4px solid';
    toast.style.background = '#f1f5f9';
    if (type === 'success') { toast.style.background = '#dcfce7'; toast.style.borderLeftColor = '#16a34a'; }
    else if (type === 'warning') { toast.style.background = '#fef3c7'; toast.style.borderLeftColor = '#f59e0b'; }
    else if (type === 'error') { toast.style.background = '#fee2e2'; toast.style.borderLeftColor = '#dc2626'; }
    else { toast.style.borderLeftColor = '#334155'; }

    container.appendChild(toast);
    setTimeout(() => {
      try {
        toast.style.transition = 'opacity 200ms ease';
        toast.style.opacity = '0';
        setTimeout(() => { toast.remove(); }, 220);
      } catch {}
    }, 3500);
  } catch {}
}

// ============ SESSION MANAGEMENT ============

function initUserSession() {
  const username = localStorage.getItem('username');
  const userNav = document.getElementById('userNav');
  
  // Redirect to login if not authenticated
  const page = document.body ? document.body.baseURI.split('/').pop().split('?')[0] : '';
  if (page !== 'login.html' && !username) {
    window.location.href = window.location.pathname.includes('/pages/') ? 'login.html' : 'pages/login.html';
    return;
  }
  
  // Show user info
  if (userNav && username) {
    userNav.innerHTML = `
      <span>Connecté: <strong>${username}</strong></span>
      <button id="logoutBtn" class="btn-logout">Déconnexion</button>
    `;
    
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        window.location.href = window.location.pathname.includes('/pages/') ? 'login.html' : 'pages/login.html';
      });
    }
  }
}

// Run session init on page load
document.addEventListener('DOMContentLoaded', initUserSession);

// ============ ACTIVITIES API ============

async function loadActivities() {
  try {
    const response = await fetch(`${API_BASE}/activites?userId=${CURRENT_USER_ID}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) {
      console.warn('Failed to load activities, using empty list');
      return [];
    }
    const data = await response.json();
    return data || [];
  } catch (e) {
    console.error('Error loading activities:', e);
    return [];
  }
}

async function saveActivities(activities) {
  // This is now handled by individual create/update/delete calls
  // Kept for compatibility
  return Promise.resolve();
}

async function createActivity(activity) {
  try {
    const params = new URLSearchParams({
      action: 'create',
      userId: CURRENT_USER_ID,
      titre: activity.titre || activity.name || '',
      description: activity.description || '',
      debut: activity.debut || activity.start || '',
      fin: activity.fin || activity.end || '',
      categorieId: activity.categorieId || '',
      priorite: activity.priorite || activity.priority || 'NORMALE',
      statut: activity.statut || 'PLANIFIE'
    });

    const response = await fetch(`${API_BASE}/activites`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });

    if (!response.ok) throw new Error('Failed to create activity');
    return await response.json();
  } catch (e) {
    console.error('Error creating activity:', e);
    throw e;
  }
}

async function updateActivity(id, activity) {
  try {
    const params = new URLSearchParams({
      action: 'update',
      id: id,
      userId: CURRENT_USER_ID,
      titre: activity.titre || activity.name || '',
      description: activity.description || '',
      debut: activity.debut || activity.start || '',
      fin: activity.fin || activity.end || '',
      categorieId: activity.categorieId || '',
      priorite: activity.priorite || activity.priority || 'NORMALE',
      statut: activity.statut || 'PLANIFIE'
    });

    const response = await fetch(`${API_BASE}/activites`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });

    if (!response.ok) throw new Error('Failed to update activity');
    return await response.json();
  } catch (e) {
    console.error('Error updating activity:', e);
    throw e;
  }
}

async function deleteActivity(id) {
  try {
    const params = new URLSearchParams({
      action: 'delete',
      id: id,
      userId: CURRENT_USER_ID
    });

    const response = await fetch(`${API_BASE}/activites`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });

    if (!response.ok) throw new Error('Failed to delete activity');
    return await response.json();
  } catch (e) {
    console.error('Error deleting activity:', e);
    throw e;
  }
}

// ============ CONSTRAINTS API ============

async function loadConstraints() {
  try {
    const response = await fetch(`${API_BASE}/contraintes?userId=${CURRENT_USER_ID}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) {
      return { minSleep: 8, unavailable: [] };
    }
    const data = await response.json();
    return data || { minSleep: 8, unavailable: [] };
  } catch (e) {
    console.error('Error loading constraints:', e);
    return { minSleep: 8, unavailable: [] };
  }
}

async function saveConstraints(constraints) {
  // This is now handled by individual create/update/delete calls
  // Kept for compatibility
  return Promise.resolve();
}

async function saveMinSleep(minSleep) {
  try {
    const params = new URLSearchParams({
      action: 'setSleep',
      userId: CURRENT_USER_ID,
      minSleep: minSleep
    });

    const response = await fetch(`${API_BASE}/contraintes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });

    if (!response.ok) throw new Error('Failed to save min sleep');
    return await response.json();
  } catch (e) {
    console.error('Error saving min sleep:', e);
    throw e;
  }
}

async function addUnavailability(unavailability) {
  try {
    const params = new URLSearchParams({
      action: 'addUnavailable',
      userId: CURRENT_USER_ID,
      day: unavailability.day || 0,
      start: unavailability.start || '00:00',
      end: unavailability.end || '23:59',
      type: unavailability.type || 'MEETING'
    });

    const response = await fetch(`${API_BASE}/contraintes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });

    if (!response.ok) throw new Error('Failed to add unavailability');
    return await response.json();
  } catch (e) {
    console.error('Error adding unavailability:', e);
    throw e;
  }
}

async function deleteUnavailability(id) {
  try {
    const params = new URLSearchParams({
      action: 'delUnavailable',
      userId: CURRENT_USER_ID,
      id: id
    });

    const response = await fetch(`${API_BASE}/contraintes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });

    if (!response.ok) throw new Error('Failed to delete unavailability');
    return await response.json();
  } catch (e) {
    console.error('Error deleting unavailability:', e);
    throw e;
  }
}
