import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, timer } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';

export interface Customer {
  id: number;
  name: string;
  email: string;
  status: 'NEW' | 'PROCESSED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

export interface JobExecution {
  jobExecutionId: number;
  jobName: string;
  status: string;
  startTime: string;
  endTime?: string;
  exitCode?: string;
  exitDescription?: string;
  failureExceptions: string[];
  stepExecutions: StepExecution[];
}

export interface StepExecution {
  stepName: string;
  status: string;
  readCount: number;
  writeCount: number;
  skipCount: number;
  commitCount: number;
  rollbackCount: number;
  exitCode: string;
  exitDescription: string;
  failureExceptions: string[];
}

export interface CustomerStats {
  totalCustomers: number;
  processedCustomers: number;
  failedCustomers: number;
  newCustomers: number;
}

@Injectable({
  providedIn: 'root'
})
export class BatchService {
  private apiUrl = 'http://localhost:8080/api/batch';
  private customersSubject = new BehaviorSubject<Customer[]>([]);
  private statsSubject = new BehaviorSubject<CustomerStats | null>(null);
  private pollingInterval = 5000; // 5 seconds

  constructor(private http: HttpClient) {
    this.startPolling();
  }

  // Start polling for updates
  private startPolling() {
    timer(0, this.pollingInterval).pipe(
      switchMap(() => this.fetchCustomersAndStats())
    ).subscribe();
  }

  // Fetch both customers and stats
  private fetchCustomersAndStats(): Observable<any> {
    return this.http.get<Customer[]>(`${this.apiUrl}/customers`).pipe(
      switchMap(customers => {
        this.customersSubject.next(customers);
        return this.http.get<CustomerStats>(`${this.apiUrl}/customers/stats`);
      }),
      map(stats => {
        this.statsSubject.next(stats);
        return { customers: this.customersSubject.value, stats };
      }),
      catchError(error => {
        console.error('Error fetching data:', error);
        return [];
      })
    );
  }

  // Get customers as observable
  getCustomers(): Observable<Customer[]> {
    return this.customersSubject.asObservable();
  }

  // Get stats as observable
  getStats(): Observable<CustomerStats | null> {
    return this.statsSubject.asObservable();
  }

  // Launch import customers job
  launchImportJob(parameters: any = {}): Observable<JobExecution> {
    return this.http.post<JobExecution>(
      `${this.apiUrl}/jobs/import-customers`,
      null,
      { params: new HttpParams({ fromObject: parameters }) }
    );
  }

  // Launch complex processing job
  launchProcessingJob(parameters: any = {}): Observable<JobExecution> {
    return this.http.post<JobExecution>(
      `${this.apiUrl}/jobs/complex-processing`,
      null,
      { params: new HttpParams({ fromObject: parameters }) }
    );
  }

  // Get job execution details
  getJobExecutionDetails(jobExecutionId: number): Observable<JobExecution> {
    return this.http.get<JobExecution>(
      `${this.apiUrl}/jobs/${jobExecutionId}/details`
    );
  }

  // Delete all customers
  deleteAllCustomers(): Observable<{ message: string; deletedCount: number }> {
    return this.http.delete<{ message: string; deletedCount: number }>(
      `${this.apiUrl}/customers`
    );
  }

  // Helper method to format job status
  formatJobStatus(status: string): string {
    return status.replace('_', ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}
