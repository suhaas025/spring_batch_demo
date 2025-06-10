import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BatchService, JobExecution } from '../../services/batch.service';
import { Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-job-details',
  templateUrl: './job-details.component.html',
  styleUrls: ['./job-details.component.css']
})
export class JobDetailsComponent implements OnInit, OnDestroy {
  jobExecutionId: number = 0;
  jobExecution: JobExecution | null = null;
  loading: boolean = true;
  error: string | null = null;
  private pollingSubscription?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private batchService: BatchService
  ) { }

  ngOnInit(): void {
    this.jobExecutionId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.jobExecutionId) {
      this.router.navigate(['/']);
      return;
    }

    this.startPolling();
  }

  ngOnDestroy(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  private startPolling(): void {
    this.pollingSubscription = timer(0, 5000).pipe(
      switchMap(() => this.batchService.getJobExecutionDetails(this.jobExecutionId))
    ).subscribe({
      next: (jobExecution) => {
        this.jobExecution = jobExecution;
        this.loading = false;
        
        // Stop polling if job is completed or failed
        if (jobExecution.status === 'COMPLETED' || jobExecution.status === 'FAILED') {
          this.pollingSubscription?.unsubscribe();
        }
      },
      error: (err) => {
        this.error = 'Failed to load job execution details. Please try again later.';
        this.loading = false;
        console.error('Error loading job execution:', err);
        this.pollingSubscription?.unsubscribe();
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'status-success';
      case 'FAILED':
        return 'status-error';
      case 'STARTED':
      case 'STARTING':
        return 'status-warning';
      default:
        return '';
    }
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleString();
  }

  getDuration(): string {
    if (!this.jobExecution?.startTime) return 'N/A';
    
    const start = new Date(this.jobExecution.startTime);
    const end = this.jobExecution.endTime ? new Date(this.jobExecution.endTime) : new Date();
    const duration = end.getTime() - start.getTime();
    
    const seconds = Math.floor(duration / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
