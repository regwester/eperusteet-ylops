alter table opetussuunnitelma
add column pohja_id int8;

alter table opetussuunnitelma_AUD
add column pohja_id int8;

alter table opetussuunnitelma
add constraint FK_tnfxq12pi5iq9h0v8g9cal80y
foreign key (pohja_id)
references opetussuunnitelma;
