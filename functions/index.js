const functions = require('firebase-functions');


   // The Firebase Admin SDK to access Cloud Firestore.
   const admin = require('firebase-admin');
   admin.initializeApp();
   exports.cleanStorage=functions.database
   .ref('/counting')
   .onUpdate((change,context)=>{
   const after=change.after.val();





   if(Number(after.count)<=20){
   return null;
    }
     const newCount= after.count-7;
     const memes_ref=change.after.ref.parent.child("memes");
     var cnt=0;
     var photorefs= [];
      memes_ref.once('value',function(snapshot){

        snapshot.forEach(function(childSnapshot){

        cnt++;
        if(cnt<=7){
         photorefs.push(childSnapshot.val());
         memes_ref.child(childSnapshot.key).remove();
        }
        });
        const bucket=admin.storage().bucket();
        for(index=0;index<photorefs.length;index++){
        try{
        bucket.file('Memes/'+photorefs[index]).delete();
        }
        catch(error){
        console.log(error);
        }
        }

      });
      const memepicture_ref=change.after.ref.parent.child("memepicture");
      memepicture_ref.once('value',function(snapshot){
       snapshot.forEach(function(childSnapshot){
         if(photorefs.indexOf(childSnapshot.key)!==-1){
         memepicture_ref.child(childSnapshot.key).remove();
         }
       });
      });



   return change.after.ref.update({count:newCount});


   });


