-- phpMyAdmin SQL Dump
-- version 2.11.11
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 25, 2014 at 01:44 PM
-- Server version: 5.0.95
-- PHP Version: 5.1.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `crawljaxsuite`
--

-- --------------------------------------------------------

--
-- Table structure for table `TestResults`
--

CREATE TABLE IF NOT EXISTS `TestResults` (
  `id` int(11) NOT NULL,
  `JsonResults` text NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `workload`
--

CREATE TABLE IF NOT EXISTS `workload` (
  `id` int(11) NOT NULL auto_increment,
  `url` varchar(255) NOT NULL,
  `worker` text NOT NULL,
  `crawled` binary(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `url` (`url`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=0 ;