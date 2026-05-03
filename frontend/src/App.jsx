import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import Profile from './pages/Profile'
import SellerDashboard from './pages/SellerDashboard'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { CartProvider } from './contexts/CartContext'
import Navbar from './components/Navbar'

function ProtectedRoute({children}){
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? children : <Navigate to="/login" replace />
}

function SellerRoute({children}){
  const { isAuthenticated, user } = useAuth()
  return (isAuthenticated && user?.isSeller) ? children : <Navigate to="/" replace />
}

export default function App(){
  return (
    <AuthProvider>
      <CartProvider>
        <Navbar />
        <div className="max-w-6xl mx-auto p-6">
          <Routes>
            <Route path="/" element={<Landing/>} />
            <Route path="/login" element={<Login/>} />
            <Route path="/register" element={<Register/>} />
            <Route path="/products/:id" element={<ProductDetail/>} />
            <Route path="/cart" element={<Cart/>} />
            <Route path="/checkout" element={<ProtectedRoute><Checkout/></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Profile/></ProtectedRoute>} />
            <Route path="/seller" element={<SellerDashboard/>} />
          </Routes>
        </div>
      </CartProvider>
    </AuthProvider>
  )
}
