import { DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Product } from '../models/product.model';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './products.component.html'
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  errorMessage = '';
  deletingId: number | null = null;

  constructor(private readonly productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible de charger les produits. Vérifiez que le backend fonctionne sur le port 8083.';
      }
    });
  }

  deleteProduct(product: Product): void {
    if (!window.confirm(`Voulez-vous supprimer le produit « ${product.name} » ?`)) {
      return;
    }

    this.deletingId = product.id;
    this.errorMessage = '';
    this.productService.deleteProduct(product).subscribe({
      next: () => {
        this.deletingId = null;
        this.loadProducts();
      },
      error: () => {
        this.deletingId = null;
        this.errorMessage = 'La suppression du produit a échoué. Veuillez réessayer.';
      }
    });
  }
}
