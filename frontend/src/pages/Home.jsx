import React, {useEffect, useState} from 'react'

export default function Home(){
  const [products, setProducts] = useState(null);
  const [err, setErr] = useState(null);

  useEffect(()=>{
    fetch('/api/products').then(async res => {
      if (!res.ok) throw new Error(await res.text());
      return res.json();
    }).then(setProducts).catch(e=>setErr(e.message));
  }, [])

  if (err) return <div className="bg-white p-4 rounded shadow">Failed to load products: {err}</div>
  if (products === null) return <div className="bg-white p-4 rounded shadow">Loading products...</div>
  if (products.length === 0) return <div className="bg-white p-4 rounded shadow">No products available</div>

  return (
    <section className="grid gap-4">
      {products.map(p => (
        <div key={p.id} className="bg-white p-4 rounded shadow">
          <h3 className="font-medium">{p.name}</h3>
          <p className="text-sm text-gray-600">{p.description}</p>
        </div>
      ))}
    </section>
  )
}
