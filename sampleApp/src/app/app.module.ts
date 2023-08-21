// app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { IonicModule } from '@ionic/angular';
import { AngularFireModule } from '@angular/fire'; // Import AngularFireModule
import { environment } from '../environments/environment'; // Import your environment configuration
import { AppComponent } from './app.component';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    IonicModule.forRoot(),
    AngularFireModule.initializeApp(environment.firebaseConfig), // Initialize AngularFireModule with firebaseConfig
    // Add other AngularFire modules if needed, e.g., AngularFirestoreModule for Firestore
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
