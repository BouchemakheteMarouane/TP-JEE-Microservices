import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  it('renders a Bootstrap welcome card and products link', async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([])]
    }).compileComponents();
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.card')).not.toBeNull();
    expect(element.querySelector('h1')?.textContent).toContain('Bienvenue');
    expect(element.querySelector('a')?.getAttribute('href')).toBe('/products');
  });
});
