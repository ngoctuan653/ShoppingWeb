// js/app.js (or inline before </body>)

const products = [
  { name: 'Classic Blue Jeans',       price: 59.99,  image: 'images/jeans.jpg'       },
  { name: 'White Cotton Shirt',       price: 29.99,  image: 'images/shirt.jpg'      },
  { name: 'Black Leather Jacket',     price: 149.99, image: 'images/jacket.jpg'     },
  { name: 'Summer Floral Dress',      price: 49.99,  image: 'images/dress.jpg'      },
  { name: 'Red Graphic Tee',          price: 19.99,  image: 'images/tee.jpg'        },
  { name: 'Slim Fit Chinos',          price: 39.99,  image: 'images/chinos.jpg'     },
  { name: 'Denim Jacket',             price: 89.99,  image: 'images/denim-jacket.jpg'},
  { name: 'Casual Sneakers',          price: 79.99,  image: 'images/sneakers.jpg'   }
];

function renderProducts() {
  const grid = document.getElementById('productsGrid');
  grid.innerHTML = products.map(p => `
    <div class="col col-3">
      <div class="product-card">
        <img src="${p.image}" alt="${p.name}" class="product-card__img">
        <div class="product-card__info">
          <h3 class="product-card__name">${p.name}</h3>
          <p class="product-card__price">$${p.price.toFixed(2)}</p>
          <button class="product-card__btn">Add to Cart</button>
        </div>
      </div>
    </div>
  `).join('');
}

document.addEventListener('DOMContentLoaded', renderProducts);
