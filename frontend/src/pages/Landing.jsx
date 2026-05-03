import React from 'react'

export default function Landing(){
  return (
    <div>
      <h2 className="text-2xl font-semibold mb-4">Welcome to Marketplace</h2>
      <p className="mb-6">Browse products from multiple sellers.</p>
      <div id="product-list" className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Product cards will be rendered here by fetch in a future step */}
      </div>
    </div>
  )
}
