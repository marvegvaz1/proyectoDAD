DELIMITER //

CREATE OR REPLACE PROCEDURE 

createtables()

BEGIN

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS actuador;
DROP TABLE IF EXISTS sensor;



CREATE TABLE actuador(
idValue INT NOT NULL AUTO_INCREMENT,
idActuador INT NOT NULL,
timeStampAcsensortuador BIGINT NOT NULL,
valueActuador INT NOT NULL,
idGrupo INT NOT NULL,
idPlaca INT NOT NULL,
PRIMARY KEY(idValue)
);

CREATE TABLE sensor(
idValue INT NOT NULL AUTO_INCREMENT,
idSensor INT NOT NULL,
timeStampSensor BIGINT NOT NULL,
valueTemp DOUBLE NOT NULL,
valueHum DOUBLE NOT NULL,
idGrupo INT NOT NULL,
idPlaca INT NOT NULL,
PRIMARY KEY(idValue)
);

SET FOREIGN_KEY_CHECKS = 1;
END //

DELIMITER ;

CALL createtables();











