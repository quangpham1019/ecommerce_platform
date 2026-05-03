async function api(path, opts = {}) {
  const base = window.location.origin.replace(/:\d+$/, '') || 'http://localhost:8080';
  const url = base + '/api' + path;
  const res = await fetch(url, Object.assign({headers:{'Content-Type':'application/json'}}, opts));
  if (!res.ok) {
    const t = await res.text();
    throw new Error(res.status + ' ' + t);
  }
  return res.json().catch(()=>null);
}

document.getElementById('btn-register').addEventListener('click', async ()=>{
  const email = document.getElementById('reg-email').value;
  const password = document.getElementById('reg-password').value;
  try {
    const user = await api('/register', {method:'POST', body: JSON.stringify({email, password})});
    localStorage.setItem('userId', user.id);
    document.getElementById('auth-msg').innerText = 'Registered as ' + user.email + ' (id=' + user.id + ')';
  } catch(e){ document.getElementById('auth-msg').innerText = 'Register failed: ' + e.message }
});

document.getElementById('btn-login').addEventListener('click', async ()=>{
  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;
  try {
    const user = await api('/login', {method:'POST', body: JSON.stringify({email, password})});
    localStorage.setItem('userId', user.id);
    document.getElementById('auth-msg').innerText = 'Logged in as ' + user.email + ' (id=' + user.id + ')';
  } catch(e){ document.getElementById('auth-msg').innerText = 'Login failed: ' + e.message }
});

async function loadProducts(){
  try{
    const list = await api('/products', {method:'GET'});
    const container = document.getElementById('product-list');
    if (!list || list.length===0) { container.innerText = 'No products'; return }
    container.innerHTML = list.map(p => `<div><strong>${p.title}</strong> - ${p.description}</div>`).join('\n');
  }catch(e){ document.getElementById('product-list').innerText = 'Failed to load: ' + e.message }
}

loadProducts();
