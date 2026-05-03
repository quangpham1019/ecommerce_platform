import React, {createContext, useContext, useState, useEffect} from 'react'

const CartContext = createContext(null)

export function CartProvider({children}){
  const [items, setItems] = useState(()=>{
    try{ return JSON.parse(localStorage.getItem('cart') || '[]') }catch(e){return []}
  })

  useEffect(()=>{ localStorage.setItem('cart', JSON.stringify(items)) }, [items])

  function add(item){
    setItems(prev=>{
      const found = prev.find(i=>i.productId===item.productId)
      if (found) return prev.map(i=> i.productId===item.productId ? {...i, qty: i.qty + item.qty} : i)
      return [...prev, item]
    })
  }
  function remove(productId){ setItems(prev=>prev.filter(i=>i.productId!==productId)) }
  function update(productId, qty){ setItems(prev=>prev.map(i=> i.productId===productId ? {...i, qty} : i)) }
  function clear(){ setItems([]) }

  return (
    <CartContext.Provider value={{items, add, remove, update, clear}}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart(){ return useContext(CartContext) }
