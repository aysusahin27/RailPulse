/**
 * RailPulse — Application Frontend
 * Clean, modern client for Train departures & railway management.
 */

// Application State
const state = {
  token: localStorage.getItem('railpulse_token') || null,
  user: JSON.parse(localStorage.getItem('railpulse_user') || 'null'),
  stations: [],
  trips: [],
  activeCity: '',
  activeFilter: 'all',
  searchDebounceTimer: null,
};

// DOM Elements
const DOM = {
  liveClock: document.getElementById('liveClock'),
  navAuthSection: document.getElementById('navAuthSection'),
  btnOpenAuthModal: document.getElementById('btnOpenAuthModal'),
  searchInput: document.getElementById('searchInput'),
  searchClearBtn: document.getElementById('searchClearBtn'),
  stationFilterSelect: document.getElementById('stationFilterSelect'),
  btnRefresh: document.getElementById('btnRefresh'),
  btnChime: document.getElementById('btnChime'),
  cityChips: document.querySelectorAll('.city-chips .chip'),
  currentStationName: document.getElementById('currentStationName'),
  currentStationUic: document.getElementById('currentStationUic'),
  statTotalTrips: document.getElementById('statTotalTrips'),
  statOnTimeRate: document.getElementById('statOnTimeRate'),
  filterTabs: document.querySelectorAll('.filter-tab'),
  adminActionButtons: document.getElementById('adminActionButtons'),
  tripsTableBody: document.getElementById('tripsTableBody'),
  toastContainer: document.getElementById('toastContainer'),

  // Modals
  authModal: document.getElementById('authModal'),
  loginForm: document.getElementById('loginForm'),
  registerForm: document.getElementById('registerForm'),
  tabBtnLogin: document.getElementById('tabBtnLogin'),
  tabBtnRegister: document.getElementById('tabBtnRegister'),
  btnQuickAdmin: document.getElementById('btnQuickAdmin'),
  btnQuickUser: document.getElementById('btnQuickUser'),

  tripModal: document.getElementById('tripModal'),
  tripForm: document.getElementById('tripForm'),
  tripModalTitle: document.getElementById('tripModalTitle'),
  tripDepartureStation: document.getElementById('tripDepartureStation'),

  stationModal: document.getElementById('stationModal'),
  stationForm: document.getElementById('stationForm'),

  detailsModal: document.getElementById('detailsModal'),
  detailsBody: document.getElementById('detailsBody'),
};

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
  initClock();
  setupEventListeners();
  updateAuthUI();
  loadStations().then(() => loadTrips());
});

// ==================== CLOCK ====================
function initClock() {
  function update() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    if (DOM.liveClock) {
      DOM.liveClock.textContent = `${hours}:${minutes}:${seconds}`;
    }
  }
  update();
  setInterval(update, 1000);
}

// ==================== API HELPERS ====================
async function apiFetch(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  try {
    const response = await fetch(endpoint, { ...options, headers });

    if (response.status === 204) {
      return null;
    }

    const data = await response.json().catch(() => null);

    if (!response.ok) {
      const errorMsg = (data && data.message) || `Erreur HTTP ${response.status}`;
      if (response.status === 401) {
        logout();
        showToast('Session expirée ou non autorisée. Veuillez vous reconnecter.', 'error');
      }
      throw new Error(errorMsg);
    }

    return data;
  } catch (error) {
    console.error(`API Error on ${endpoint}:`, error);
    throw error;
  }
}

// ==================== DATA LOADERS ====================
async function loadStations() {
  try {
    const stations = await apiFetch('/stations');
    state.stations = stations || [];
    populateStationSelect();
    return state.stations;
  } catch (err) {
    showToast('Erreur lors du chargement des gares', 'error');
    return [];
  }
}

async function loadTrips(searchQuery = '', selectedStationId = null) {
  renderLoadingState();
  try {
    let url = '/trips';
    const stId = selectedStationId !== null ? selectedStationId : (DOM.stationFilterSelect ? DOM.stationFilterSelect.value : '');

    if (stId) {
      url = `/stations/${stId}/trips`;
    } else {
      const query = searchQuery.trim() || state.activeCity.trim();
      if (query) {
        url = `/trips/search?q=${encodeURIComponent(query)}`;
      }
    }

    const trips = await apiFetch(url);
    state.trips = trips || [];
    renderBoard();
  } catch (err) {
    renderEmptyState('Impossible de charger les trains en direct');
    showToast(err.message, 'error');
  }
}

function populateStationSelect() {
  const optionsHtml = state.stations.map(st => `
    <option value="${st.id}">${escapeHtml(st.name)} (${escapeHtml(st.city || '')})</option>
  `).join('');

  if (DOM.tripDepartureStation) {
    DOM.tripDepartureStation.innerHTML = optionsHtml;
  }

  if (DOM.stationFilterSelect) {
    DOM.stationFilterSelect.innerHTML = '<option value="">Toutes les gares de départ</option>' + optionsHtml;
  }
}

// ==================== RENDERING ====================
function renderBoard() {
  let filtered = [...state.trips];

  // Tab filter
  if (state.activeFilter === 'TGV') {
    filtered = filtered.filter(t => t.trainType === 'TGV INOUI' || t.trainType === 'OUIGO');
  } else if (state.activeFilter === 'TER') {
    filtered = filtered.filter(t => t.trainType === 'TER');
  } else if (state.activeFilter === 'ON_TIME') {
    filtered = filtered.filter(t => t.status === 'ON_TIME');
  }

  // Update Station Title
  if (state.activeCity) {
    DOM.currentStationName.textContent = `Gares de ${state.activeCity}`;
    DOM.currentStationUic.textContent = `CITY-${state.activeCity.toUpperCase()}`;
  } else if (DOM.searchInput.value.trim()) {
    DOM.currentStationName.textContent = `Résultats pour "${DOM.searchInput.value.trim()}"`;
    DOM.currentStationUic.textContent = 'SEARCH';
  } else {
    DOM.currentStationName.textContent = 'Réseau National — Tous les départs';
    DOM.currentStationUic.textContent = 'FR-NETWORK';
  }

  // Update Stats
  const total = filtered.length;
  const onTimeCount = filtered.filter(t => t.status === 'ON_TIME').length;
  const onTimeRate = total > 0 ? Math.round((onTimeCount / total) * 100) : 100;

  DOM.statTotalTrips.textContent = total;
  DOM.statOnTimeRate.textContent = `${onTimeRate}%`;

  if (filtered.length === 0) {
    renderEmptyState('Aucun train correspondant trouvé');
    return;
  }

  DOM.tripsTableBody.innerHTML = filtered.map(trip => {
    const badgeClass = getBadgeClass(trip.trainType);
    const statusInfo = getStatusInfo(trip.status, trip.statusMessage);

    const isAuth = !!state.token;
    const adminActions = isAuth ? `
      <button class="btn-icon" title="Modifier" onclick="openEditTripModal(${trip.id})">✏️</button>
      <button class="btn-icon btn-icon-danger" title="Supprimer" onclick="handleDeleteTrip(${trip.id})">🗑️</button>
    ` : '';

    return `
      <tr>
        <td class="cell-time">${escapeHtml(trip.departureTime || '--:--')}</td>
        <td>
          <div class="cell-train">
            <span class="train-number">${escapeHtml(trip.label)}</span>
            <span class="train-badge ${badgeClass}">${escapeHtml(trip.trainType || 'TRAIN')}</span>
          </div>
        </td>
        <td>
          <div class="cell-destination">${escapeHtml(trip.destinationName)}</div>
          <div class="destination-sub">${trip.arrivalTime ? 'Arrivée prev. ' + escapeHtml(trip.arrivalTime) : 'Direct'}</div>
        </td>
        <td>
          <span style="font-weight: 600; color: #cbd5e1;">${escapeHtml(trip.departureStation?.name || 'Inconnue')}</span>
          <div style="font-size: 0.775rem; color: #64748b;">${escapeHtml(trip.departureStation?.city || '')}</div>
        </td>
        <td>
          <span class="cell-track ${trip.track ? 'track-announced' : ''}">${escapeHtml(trip.track || 'Affichage')}</span>
        </td>
        <td>
          <span class="status-pill ${statusInfo.className}">
            <span>${statusInfo.icon}</span>
            <span>${escapeHtml(statusInfo.text)}</span>
          </span>
        </td>
        <td style="text-align: right;">
          <div class="btn-actions" style="justify-content: flex-end;">
            <button class="btn-icon" title="Voir les détails" onclick="openTripDetails(${trip.id})">ℹ️</button>
            ${adminActions}
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function getBadgeClass(type) {
  if (!type) return 'badge-tgv';
  const t = type.toUpperCase();
  if (t.includes('TGV')) return 'badge-tgv';
  if (t.includes('OUIGO')) return 'badge-ouigo';
  if (t.includes('TER')) return 'badge-ter';
  if (t.includes('INTERCIT')) return 'badge-intercites';
  if (t.includes('EUROSTAR')) return 'badge-eurostar';
  return 'badge-tgv';
}

function getStatusInfo(status, message) {
  switch (status) {
    case 'ON_TIME':
      return { className: 'status-on_time', icon: '●', text: message || "À l'heure" };
    case 'DELAYED':
      return { className: 'status-delayed', icon: '▲', text: message || 'Retardé' };
    case 'BOARDING':
      return { className: 'status-boarding', icon: '■', text: message || 'Embarquement' };
    case 'CANCELLED':
      return { className: 'status-cancelled', icon: '✕', text: message || 'Supprimé' };
    default:
      return { className: 'status-on_time', icon: '●', text: message || "À l'heure" };
  }
}

function renderLoadingState() {
  DOM.tripsTableBody.innerHTML = `
    <tr>
      <td colspan="7" class="empty-state">
        <div class="loading-spinner"></div>
        <p style="margin-top: 1rem;">Mise à jour des horaires en temps réel...</p>
      </td>
    </tr>
  `;
}

function renderEmptyState(message) {
  DOM.tripsTableBody.innerHTML = `
    <tr>
      <td colspan="7" class="empty-state">
        <div class="empty-icon">🚉</div>
        <h3>${escapeHtml(message)}</h3>
        <p>Essayez de modifier votre recherche ou de sélectionner une autre ville.</p>
      </td>
    </tr>
  `;
}

// ==================== AUTH UI ====================
function updateAuthUI() {
  if (state.token && state.user) {
    const roleLabel = state.user.role === 'ROLE_ADMIN' ? 'Chef de Gare' : 'Voyageur';
    DOM.navAuthSection.innerHTML = `
      <div class="user-badge">
        <div class="user-avatar">${escapeHtml(state.user.username.charAt(0).toUpperCase())}</div>
        <span>${escapeHtml(state.user.fullName || state.user.username)}</span>
        <span class="user-role-badge">${roleLabel}</span>
      </div>
      <button class="btn btn-outline btn-sm" id="btnLogoutBtn" title="Se déconnecter">
        <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
      </button>
    `;

    DOM.adminActionButtons.innerHTML = `
      <button class="btn btn-primary btn-sm" id="btnOpenTripModal">
        <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
        Nouveau Train
      </button>
      <button class="btn btn-outline btn-sm" id="btnOpenStationModal">
        <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
        Nouvelle Gare
      </button>
    `;

    document.getElementById('btnLogoutBtn')?.addEventListener('click', logout);
    document.getElementById('btnOpenTripModal')?.addEventListener('click', () => openCreateTripModal());
    document.getElementById('btnOpenStationModal')?.addEventListener('click', () => openCreateStationModal());
  } else {
    DOM.navAuthSection.innerHTML = `
      <button class="btn btn-outline btn-sm" id="btnOpenAuthModal">
        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/></svg>
        Connexion
      </button>
    `;
    DOM.adminActionButtons.innerHTML = '';
    document.getElementById('btnOpenAuthModal')?.addEventListener('click', () => openModal('authModal'));
  }
}

function setSession(authResponse) {
  state.token = authResponse.token;
  state.user = {
    userId: authResponse.userId,
    username: authResponse.username,
    email: authResponse.email,
    fullName: authResponse.fullName,
    role: authResponse.role,
  };
  localStorage.setItem('railpulse_token', state.token);
  localStorage.setItem('railpulse_user', JSON.stringify(state.user));
  updateAuthUI();
  renderBoard();
  showToast(`Bienvenue, ${state.user.fullName || state.user.username} !`, 'success');
}

function logout() {
  state.token = null;
  state.user = null;
  localStorage.removeItem('railpulse_token');
  localStorage.removeItem('railpulse_user');
  updateAuthUI();
  renderBoard();
  showToast('Déconnexion réussie.', 'info');
}

// ==================== EVENT LISTENERS ====================
function setupEventListeners() {
  // Search input with debounce
  DOM.searchInput?.addEventListener('input', (e) => {
    const val = e.target.value;
    DOM.searchClearBtn.style.display = val ? 'block' : 'none';

    clearTimeout(state.searchDebounceTimer);
    state.searchDebounceTimer = setTimeout(() => {
      state.activeCity = '';
      DOM.cityChips.forEach(c => c.classList.remove('active'));
      loadTrips(val);
    }, 250);
  });

  DOM.searchClearBtn?.addEventListener('click', () => {
    DOM.searchInput.value = '';
    DOM.searchClearBtn.style.display = 'none';
    state.activeCity = '';
    DOM.cityChips[0]?.classList.add('active');
    loadTrips();
  });

  // Station dropdown filter
  DOM.stationFilterSelect?.addEventListener('change', (e) => {
    const stId = e.target.value;
    state.activeCity = '';
    DOM.searchInput.value = '';
    DOM.cityChips.forEach(c => c.classList.remove('active'));
    loadTrips('', stId);
  });

  // Sound Chime Button
  DOM.btnChime?.addEventListener('click', () => {
    playStationChime();
  });

  // Refresh Button
  DOM.btnRefresh?.addEventListener('click', () => {
    loadStations().then(() => loadTrips(DOM.searchInput.value));
    showToast('Tableau des départs actualisé.', 'info');
  });

  // City Chips
  DOM.cityChips.forEach(chip => {
    chip.addEventListener('click', () => {
      DOM.cityChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');

      const city = chip.getAttribute('data-city');
      state.activeCity = city;
      DOM.searchInput.value = '';
      DOM.searchClearBtn.style.display = 'none';
      loadTrips();
    });
  });

  // Filter Tabs
  DOM.filterTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      DOM.filterTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      state.activeFilter = tab.getAttribute('data-filter');
      renderBoard();
    });
  });

  // Close modals
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modalId = btn.getAttribute('data-close-modal');
      closeModal(modalId);
    });
  });

  // Auth Tabs (Login / Register)
  DOM.tabBtnLogin?.addEventListener('click', () => {
    DOM.tabBtnLogin.classList.add('active');
    DOM.tabBtnRegister.classList.remove('active');
    DOM.loginForm.style.display = 'block';
    DOM.registerForm.style.display = 'none';
  });

  DOM.tabBtnRegister?.addEventListener('click', () => {
    DOM.tabBtnRegister.classList.add('active');
    DOM.tabBtnLogin.classList.remove('active');
    DOM.registerForm.style.display = 'block';
    DOM.loginForm.style.display = 'none';
  });

  // Quick Login Buttons
  DOM.btnQuickAdmin?.addEventListener('click', () => {
    document.getElementById('loginUsername').value = 'admin';
    document.getElementById('loginPassword').value = 'admin123';
    DOM.loginForm.dispatchEvent(new Event('submit'));
  });

  DOM.btnQuickUser?.addEventListener('click', () => {
    document.getElementById('loginUsername').value = 'voyageur';
    document.getElementById('loginPassword').value = 'password123';
    DOM.loginForm.dispatchEvent(new Event('submit'));
  });

  // Login Form Submit
  DOM.loginForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;

    try {
      const res = await apiFetch('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      setSession(res);
      closeModal('authModal');
    } catch (err) {
      showToast(err.message || 'Identifiants invalides', 'error');
    }
  });

  // Register Form Submit
  DOM.registerForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const fullName = document.getElementById('regFullName').value.trim();
    const password = document.getElementById('regPassword').value;

    try {
      const res = await apiFetch('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password, fullName }),
      });
      setSession(res);
      closeModal('authModal');
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Trip Form Submit
  DOM.tripForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const editId = document.getElementById('tripEditId').value;
    const body = {
      label: document.getElementById('tripLabel').value.trim(),
      trainType: document.getElementById('tripTrainType').value,
      departureStationId: LongOrNumber(document.getElementById('tripDepartureStation').value),
      destinationName: document.getElementById('tripDestination').value.trim(),
      departureTime: document.getElementById('tripDepartureTime').value,
      track: document.getElementById('tripTrack').value.trim(),
      status: document.getElementById('tripStatus').value,
      statusMessage: document.getElementById('tripStatusMessage').value.trim() || null,
    };

    try {
      if (editId) {
        await apiFetch(`/trips/${editId}`, {
          method: 'PUT',
          body: JSON.stringify(body),
        });
        showToast('Train mis à jour avec succès.', 'success');
      } else {
        await apiFetch('/trips', {
          method: 'POST',
          body: JSON.stringify(body),
        });
        showToast('Nouveau train programmé.', 'success');
      }
      closeModal('tripModal');
      loadTrips(DOM.searchInput.value);
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Station Form Submit
  DOM.stationForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
      name: document.getElementById('stationName').value.trim(),
      city: document.getElementById('stationCity').value.trim(),
      uicCode: document.getElementById('stationUic').value.trim(),
    };

    try {
      await apiFetch('/stations', {
        method: 'POST',
        body: JSON.stringify(body),
      });
      showToast('Gare ajoutée avec succès.', 'success');
      closeModal('stationModal');
      await loadStations();
      loadTrips();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });
}

// ==================== MODAL HELPERS ====================
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.add('open');
  }
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.remove('open');
  }
}

function openCreateTripModal() {
  document.getElementById('tripEditId').value = '';
  DOM.tripModalTitle.textContent = 'Programmer un Nouveau Train';
  DOM.tripForm.reset();
  document.getElementById('tripDepartureTime').value = '10:00';
  document.getElementById('tripTrack').value = 'Voie 1';
  openModal('tripModal');
}

window.openEditTripModal = function(id) {
  const trip = state.trips.find(t => t.id === id);
  if (!trip) return;

  document.getElementById('tripEditId').value = trip.id;
  DOM.tripModalTitle.textContent = `Modifier Train ${trip.label}`;
  document.getElementById('tripLabel').value = trip.label;
  document.getElementById('tripTrainType').value = trip.trainType || 'TGV INOUI';
  document.getElementById('tripDepartureStation').value = trip.departureStation?.id || '';
  document.getElementById('tripDestination').value = trip.destinationName;
  document.getElementById('tripDepartureTime').value = trip.departureTime || '12:00';
  document.getElementById('tripTrack').value = trip.track || 'Voie 1';
  document.getElementById('tripStatus').value = trip.status || 'ON_TIME';
  document.getElementById('tripStatusMessage').value = trip.statusMessage || '';

  openModal('tripModal');
};

function openCreateStationModal() {
  DOM.stationForm.reset();
  openModal('stationModal');
}

window.openTripDetails = function(id) {
  const trip = state.trips.find(t => t.id === id);
  if (!trip) return;

  const statusInfo = getStatusInfo(trip.status, trip.statusMessage);
  DOM.detailsBody.innerHTML = `
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.25rem;">
      <div>
        <h2 style="font-size: 1.5rem; font-weight: 800; color: #fff;">${escapeHtml(trip.label)}</h2>
        <span class="train-badge ${getBadgeClass(trip.trainType)}" style="margin-top: 0.25rem;">${escapeHtml(trip.trainType)}</span>
      </div>
      <span class="status-pill ${statusInfo.className}">${statusInfo.icon} ${escapeHtml(statusInfo.text)}</span>
    </div>

    <!-- Visual Route Timeline -->
    <div class="route-timeline">
      <div class="route-stop">
        <div class="stop-dot"></div>
        <div style="font-size: 0.8rem; font-weight: 700; color: #fbbf24;">${escapeHtml(trip.departureTime)}</div>
        <div style="font-size: 0.85rem; font-weight: 600;">${escapeHtml(trip.departureStation?.name || 'Départ')}</div>
      </div>
      <div class="route-stop">
        <div class="stop-dot stop-dot-end"></div>
        <div style="font-size: 0.8rem; font-weight: 700; color: #38bdf8;">${escapeHtml(trip.arrivalTime || 'Terminus')}</div>
        <div style="font-size: 0.85rem; font-weight: 600;">${escapeHtml(trip.destinationName)}</div>
      </div>
    </div>

    <div style="background: rgba(10, 15, 29, 0.6); padding: 1.15rem; border-radius: 8px; border: 1px solid rgba(148, 163, 184, 0.1); margin-bottom: 1rem;">
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; font-size: 0.875rem;">
        <div>Voie de départ : <strong style="color: #fbbf24; font-family: monospace;">${escapeHtml(trip.track || 'Affichage gare')}</strong></div>
        <div>Statut du train : <strong style="color: #fff;">${escapeHtml(statusInfo.text)}</strong></div>
        <div>Ville de départ : <strong>${escapeHtml(trip.departureStation?.city || '')}</strong></div>
        <div>Code UIC Gare : <code style="font-family: monospace; color: #94a3b8;">${escapeHtml(trip.departureStation?.uicCode || '')}</code></div>
      </div>
    </div>
  `;
  openModal('detailsModal');
};

function playStationChime() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();

    const notes = [
      { freq: 523.25, time: 0.0, dur: 0.35 },  // C5
      { freq: 659.25, time: 0.28, dur: 0.35 }, // E5
      { freq: 783.99, time: 0.56, dur: 0.35 }, // G5
      { freq: 1046.50, time: 0.84, dur: 0.6 }  // C6
    ];

    notes.forEach(note => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(note.freq, ctx.currentTime + note.time);

      gain.gain.setValueAtTime(0.001, ctx.currentTime + note.time);
      gain.gain.exponentialRampToValueAtTime(0.2, ctx.currentTime + note.time + 0.05);
      gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + note.time + note.dur);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(ctx.currentTime + note.time);
      osc.stop(ctx.currentTime + note.time + note.dur);
    });
    showToast('Jingle d\'annonce de gare diffusé 🔔', 'info');
  } catch (e) {
    console.warn('Audio non autorisé par la politique du navigateur', e);
  }
}

window.handleDeleteTrip = async function(id) {
  if (!confirm('Êtes-vous sûr de vouloir supprimer ce train ?')) return;

  try {
    await apiFetch(`/trips/${id}`, { method: 'DELETE' });
    showToast('Train supprimé du tableau.', 'info');
    loadTrips(DOM.searchInput.value);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

// ==================== TOAST NOTIFICATIONS ====================
function showToast(message, type = 'info') {
  if (!DOM.toastContainer) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `
    <span>${icon}</span>
    <span style="flex: 1;">${escapeHtml(message)}</span>
  `;

  DOM.toastContainer.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ==================== UTILITIES ====================
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function LongOrNumber(val) {
  return val ? parseInt(val, 10) : null;
}
