import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, IconButton, Card, CardMedia, CardContent, CardActions, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Favorite, FavoriteBorder, Public, PublicOff } from '@mui/icons-material';
import axios from 'axios';

const PersonalGallery = () => {
  const [images, setImages] = useState([]);
  const [selectedImage, setSelectedImage] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    fetchPersonalImages();
  }, [token, navigate]);

  const fetchPersonalImages = async () => {
    try {
      const response = await axios.get(`http://localhost:8009/images/personal?token=${token}`);
      setImages(response.data);
    } catch (error) {
      console.error('Erreur lors de la récupération des images personnelles:', error);
    }
  };

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedImage(null);
  };

  const toggleFavorite = async (imageId) => {
    try {
      await axios.put(`http://localhost:8009/images/${imageId}/favorite?token=${token}`);
      fetchPersonalImages();
    } catch (error) {
      console.error('Erreur lors de la modification du favori:', error);
    }
  };

  const togglePublish = async (imageId) => {
    try {
      await axios.put(`http://localhost:8009/images/${imageId}/publish?token=${token}`);
      fetchPersonalImages();
    } catch (error) {
      console.error('Erreur lors de la publication:', error);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Ma Galerie Personnelle
      </Typography>
      <Grid container spacing={3}>
        {images.map((image) => (
          <Grid item xs={12} sm={6} md={4} key={image.id}>
            <Card>
              <CardMedia
                component="img"
                height="200"
                image={`http://localhost:8009/images/${image.id}/data`}
                alt={image.name}
                onClick={() => handleImageClick(image)}
                sx={{ cursor: 'pointer' }}
              />
              <CardContent>
                <Typography variant="h6" noWrap>
                  {image.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {image.format} - {(image.size / 1024).toFixed(2)} KB
                </Typography>
              </CardContent>
              <CardActions>
                <IconButton onClick={() => toggleFavorite(image.id)}>
                  {image.favorite ? <Favorite color="error" /> : <FavoriteBorder />}
                </IconButton>
                <IconButton onClick={() => togglePublish(image.id)}>
                  {image.published ? <Public color="primary" /> : <PublicOff />}
                </IconButton>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        {selectedImage && (
          <>
            <DialogTitle>{selectedImage.name}</DialogTitle>
            <DialogContent>
              <img
                src={`http://localhost:8009/images/${selectedImage.id}/data`}
                alt={selectedImage.name}
                style={{ width: '100%', height: 'auto' }}
              />
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseDialog}>Fermer</Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  );
};

export default PersonalGallery; 