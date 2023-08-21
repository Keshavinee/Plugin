import { Component } from '@angular/core';
import { IonicModule } from '@ionic/angular';
import * as firebase from 'firebase/app';
import 'firebase/auth'; // Import specific Firebase services you need

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  standalone: true,
  imports: [IonicModule],
})
export class AppComponent {
  constructor() {
	// Your Firebase configuration obtained from the Firebase console
	const firebaseConfig = {
  	// Your configuration here
	apiKey: "AIzaSyDMWBng4X2FeegVNIc35ePXBS7w5xHK_dM",
  	authDomain: "my-firebase-project-57a96.firebaseapp.com",
  	projectId: "my-firebase-project-57a96",
  	storageBucket: "my-firebase-project-57a96.appspot.com",
  	messagingSenderId: "973693996824",
  	appId: "1:973693996824:android:dc1ca40ad16a9c7ed9586d",
	};

	// Initialize Firebase
	firebase.initializeApp(firebaseConfig);


}
}
