import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Product } from '../models/product.model';
import { ProductService } from './product.service';

describe('ProductService', () => {
  let service: ProductService;
  let httpTesting: HttpTestingController;
  const product: Product = { id: 1, name: 'Computer', price: 1300, quantity: 10, selected: true };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProductService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('gets all products from the Spring Boot API', () => {
    service.getAllProducts().subscribe(products => expect(products).toEqual([product]));
    const request = httpTesting.expectOne('http://localhost:8083/products');
    expect(request.request.method).toBe('GET');
    request.flush([product]);
  });

  it('deletes the product using its identifier', () => {
    let completed = false;
    service.deleteProduct(product).subscribe(() => completed = true);
    const request = httpTesting.expectOne('http://localhost:8083/products/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    expect(completed).toBeTrue();
  });
});
