import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { BatchService, Customer, CustomerStats, JobExecution } from '../../services/batch.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-job-list',
  templateUrl: './job-list.component.html',
  styleUrls: ['./job-list.component.css']
})
export class JobListComponent implements OnInit, OnDestroy {
  customers: Customer[] = [];
  stats: CustomerStats | null = null;
  loading: boolean = true;
  error: string | null = null;
  activeJobs: JobExecution[] = [];
  private subscriptions: Subscription[] = [];

  constructor(
    private batchService: BatchService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    // Subscribe to customers
    this.subscriptions.push(
      this.batchService.getCustomers().subscribe({
        next: (customers) => {
          this.customers = customers;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load customers. Please try again later.';
          this.loading = false;
          console.error('Error loading customers:', err);
        }
      })
    );

    // Subscribe to stats
    this.subscriptions.push(
      this.batchService.getStats().subscribe({
        next: (stats) => {
          this.stats = stats;
        },
        error: (err) => {
          console.error('Error loading stats:', err);
        }
      })
    );
  }

  launchImportJob(): void {
    this.loading = true;
    this.batchService.launchImportJob().subscribe({
      next: (response) => {
        console.log('Import job started:', response);
        this.loadData();
      },
      error: (err) => {
        this.error = 'Failed to start import job. Please try again.';
        console.error('Error starting import job:', err);
        this.loading = false;
      }
    });
  }

  launchProcessingJob(): void {
    this.loading = true;
    this.batchService.launchProcessingJob().subscribe({
      next: (response) => {
        console.log('Processing job started:', response);
        this.loadData();
      },
      error: (err) => {
        this.error = 'Failed to start processing job. Please try again.';
        console.error('Error starting processing job:', err);
        this.loading = false;
      }
    });
  }

  deleteAllCustomers(): void {
    if (confirm('Are you sure you want to delete all customers? This action cannot be undone.')) {
      this.loading = true;
      this.batchService.deleteAllCustomers().subscribe({
        next: (response) => {
          console.log('Customers deleted:', response);
          this.loadData();
        },
        error: (err) => {
          this.error = 'Failed to delete customers. Please try again.';
          console.error('Error deleting customers:', err);
          this.loading = false;
        }
      });
    }
  }

  viewJobDetails(jobExecutionId: number): void {
    this.router.navigate(['/jobs', jobExecutionId]);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PROCESSED':
        return 'status-success';
      case 'FAILED':
        return 'status-error';
      case 'NEW':
        return 'status-warning';
      default:
        return '';
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString();
  }
}
