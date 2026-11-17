package royaume.model.batiment;

public class ManuelUtilisateur {

}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
//////////				  												     	//////////
//////////				  											         	//////////
//////////				  	MANUEL UTILISATEUR BATIMENTS				     	//////////
//////////				  												     	//////////
//////////				  												     	//////////
//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////

//							 CONSTRUIRE UN BÂTIMENT

//  - Créer directement le bâtiment en utilisant : "new"
// 		mettre le nom, le niveau, les pv, les pvMax et enConstruction=true
//
// !! UNIQUEMENT POUR LES TESTS !!

//  - Créer directement le bâtiment via BatimentFactory en utilisant : "creerBatimentProduction"
//		mettre le nom du royaume, son id, le nom du bâtiment, ses pv, son type de ressource, sa productionBaseHeure et ses ouvriers 
// 		un chantier va donc se créer, il faudra alors appeler la fonction finaliser()



/////////////////////////////////////////////////////////////////////////////////////////

//							AMELIORER UN BÂTIMENT

// - appeler ameliorer() pour lancer l'amélioration
// 		mettre le nombre d'ouvriers qui vont effectuer l'amélioration et le royaume 
// - appeler finaliser() qui se lance une fois que le temps de construction est dépassé
//		cette fonction va appeler acheverAmelioration() qui va améliorer le niveau et augmenter les pv au max

// !! Quand on améliore, il est automatiquement vérifié que le niveau d'amélioration du bâtiment ne dépasse pas le niveau du chateau



/////////////////////////////////////////////////////////////////////////////////////////

// 							AMELIORER LE CHATEAU

// il faut passer le royaume au niveau suivant via ameliorerNiveau() et le château passera automatiquement au niveau suivant



/////////////////////////////////////////////////////////////////////////////////////////

// 							REPARER UN BÂTIMENT

// - appeler la focntion reparer()
// 		mettre le montant de la réparation à effectuer, le nombre d'ouvriers qui vont effectuer la réparation et le royaume



/////////////////////////////////////////////////////////////////////////////////////////

// 							DETRUIRE UN BÂTIMENT

// - appeler recevoirDegats() depuis le Royaume
//		cette fonction appelle subirDegat() qui gère la suppression du château si éventuellement les pv=0 --> destruction

// !! Ne pas utiliser la fonction subirDegat() qui est dans Batiment 


/////////////////////////////////////////////////////////////////////////////////////////



