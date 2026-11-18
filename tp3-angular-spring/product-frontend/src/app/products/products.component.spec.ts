import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Product } from '../models/product.model';
import { ProductService } from '../services/product.service';
import { ProductsComponent } from './products.component';

describe('ProductsComponent', () => {
  let fixture: ComponentFixture<ProductsComponent>;
  let component: ProductsComponent;
  let service: jasmine.SpyObj<ProductService>;
  const products: Product[] = [
    { id: 1, name: 'Computer', price: 1300, quantity: 10, selected: true },
    { id: 2, name: 'Printer', price: 450, quantity: 20, selected: false },
    { id: 3, name: 'Smartphone', price: 900, quantity: 15, selected: true }
  ];

  beforeEach(async () => {
    service = jasmine.createSpyObj<ProductService>('ProductService', ['getAllProducts', 'deleteProduct']);
    service.getAllProducts.and.returnValue(of(products));
    await TestBed.configureTestingModule({
      imports: [ProductsComponent],
      providers: [{ provide: ProductService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(ProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads products and renders their selection icons', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(service.getAllProducts).toHaveBeenCalledTimes(1);
    expect(element.querySelectorAll('tbody tr').length).toBe(3);
    expect(element.querySelectorAll('.bi-check-circle').length).toBe(2);
    expect(element.querySelectorAll('.bi-circle').length).toBe(1);
  });

  it('does not delete a product when confirmation is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteProduct(products[0]);
    expect(service.deleteProduct).not.toHaveBeenCalled();
    expect(component.products.length).toBe(3);
  });

  it('deletes a confirmed product and reloads the list', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    service.deleteProduct.and.returnValue(of(undefined));
    service.getAllProducts.and.returnValue(of(products.slice(1)));
    component.deleteProduct(products[0]);
    fixture.detectChanges();
    expect(service.deleteProduct).toHaveBeenCalledWith(products[0]);
    expect(service.getAllProducts).toHaveBeenCalledTimes(2);
    expect(component.products.length).toBe(2);
    expect(component.deletingId).toBeNull();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr').length).toBe(2);
  });

  it('shows a helpful message when the API cannot be reached', () => {
    service.getAllProducts.and.returnValue(throwError(() => new Error('Network error')));
    component.loadProducts();
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent)
      .toContain('port 8083');
  });

  it('keeps the list and re-enables deletion after an API error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    service.deleteProduct.and.returnValue(throwError(() => new Error('Deletion failed')));
    component.deleteProduct(products[0]);
    expect(component.products.length).toBe(3);
    expect(component.deletingId).toBeNull();
    expect(component.errorMessage).toContain('suppression');
  });
});
